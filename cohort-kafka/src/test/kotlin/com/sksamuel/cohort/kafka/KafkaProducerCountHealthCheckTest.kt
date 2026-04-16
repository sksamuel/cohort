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
})
