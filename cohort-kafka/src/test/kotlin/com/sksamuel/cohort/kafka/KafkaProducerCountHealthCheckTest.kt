package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.Metric
import org.apache.kafka.common.MetricName

class KafkaProducerCountHealthCheckTest : FunSpec({

   fun producer(vararg totals: Double): Producer<*, *> {
      val producer = mockk<Producer<*, *>>()
      val metricName = MetricName("record-send-total", "producer-metrics", "", emptyMap())
      val responses = totals.map { value ->
         val metric = mockk<Metric>()
         every { metric.metricName() } returns metricName
         every { metric.metricValue() } returns value
         mapOf(metricName to metric)
      }
      every { producer.metrics() } returnsMany responses
      return producer
   }

   test("returns healthy on the first check (initial scan)") {
      val check = KafkaProducerCountHealthCheck(producer(50.0), min = 10)
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("returns healthy when diff since last check meets the minimum") {
      val check = KafkaProducerCountHealthCheck(producer(50.0, 100.0), min = 10)
      check.check().status shouldBe HealthStatus.Healthy  // initial scan
      check.check().status shouldBe HealthStatus.Healthy  // diff=50 >= 10
   }

   test("returns unhealthy when diff since last check is below minimum") {
      val check = KafkaProducerCountHealthCheck(producer(50.0, 53.0), min = 10)
      check.check().status shouldBe HealthStatus.Healthy   // initial scan
      check.check().status shouldBe HealthStatus.Unhealthy // diff=3 < 10
   }

   test("diff is measured relative to the previous observation, not from zero") {
      // Totals: 100 (initial), 115 (diff=15 OK), 117 (diff=2 fail), 130 (diff=13 OK)
      val check = KafkaProducerCountHealthCheck(producer(100.0, 115.0, 117.0, 130.0), min = 10)
      check.check().status shouldBe HealthStatus.Healthy   // initial
      check.check().status shouldBe HealthStatus.Healthy   // diff=15
      check.check().status shouldBe HealthStatus.Unhealthy // diff=2
      check.check().status shouldBe HealthStatus.Healthy   // diff=13
   }

   test("prefers the aggregate (least-tagged) metric over per-topic ones") {
      // record-send-total is registered at producer level (1 tag: client-id) and per-topic
      // (2 tags: client-id, topic). Verify the producer-level metric is picked: the aggregate
      // delta meets the minimum, but any single per-topic delta is below it.
      val producer = mockk<Producer<*, *>>()
      val producerLevel = MetricName("record-send-total", "producer-metrics", "", mapOf("client-id" to "p1"))
      val perTopicA = MetricName("record-send-total", "producer-topic-metrics", "", mapOf("client-id" to "p1", "topic" to "a"))

      fun metric(metricName: MetricName, value: Double) = mockk<Metric>().also {
         every { it.metricName() } returns metricName
         every { it.metricValue() } returns value
      }

      // Iteration order puts a per-topic metric first so a firstOrNull-style picker grabs the
      // wrong metric.
      val snapshot1 = mapOf(
         perTopicA to metric(perTopicA, 10.0),
         producerLevel to metric(producerLevel, 1000.0),
      )
      val snapshot2 = mapOf(
         perTopicA to metric(perTopicA, 30.0),       // per-topic delta = 20
         producerLevel to metric(producerLevel, 1100.0), // producer-level delta = 100
      )
      every { producer.metrics() } returnsMany listOf(snapshot1, snapshot2)

      // min = 50 distinguishes the two: 100 >= 50 (aggregate, healthy) vs 20 < 50 (per-topic, unhealthy).
      val check = KafkaProducerCountHealthCheck(producer, min = 50)
      check.check().status shouldBe HealthStatus.Healthy // initial scan
      check.check().status shouldBe HealthStatus.Healthy // aggregate diff = 100 >= 50
   }
})
