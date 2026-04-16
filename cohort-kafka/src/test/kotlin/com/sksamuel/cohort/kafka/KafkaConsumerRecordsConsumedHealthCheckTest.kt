package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.Metric
import org.apache.kafka.common.MetricName

class KafkaConsumerRecordsConsumedHealthCheckTest : FunSpec({

   fun consumer(vararg totals: Double): Consumer<*, *> {
      val consumer = mockk<Consumer<*, *>>()
      val metricName = MetricName("records-consumed-total", "consumer-fetch-manager-metrics", "", emptyMap())
      val responses = totals.map { value ->
         val metric = mockk<Metric>()
         every { metric.metricName() } returns metricName
         every { metric.metricValue() } returns value
         mapOf(metricName to metric)
      }
      every { consumer.metrics() } returnsMany responses
      return consumer
   }

   test("returns healthy when total is zero (consumer not yet started)") {
      val check = KafkaConsumerRecordsConsumedHealthCheck(consumer(0.0), minRecords = 5)
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("returns healthy when diff since last check meets the minimum") {
      // first call: total=0 (startup grace), second call: diff=100 >= 5
      val check = KafkaConsumerRecordsConsumedHealthCheck(consumer(0.0, 100.0), minRecords = 5)
      check.check().status shouldBe HealthStatus.Healthy
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when diff since last check is below minimum") {
      // first call: total=0 (grace), second: diff=100 OK, third: diff=2 < 5
      val check = KafkaConsumerRecordsConsumedHealthCheck(consumer(0.0, 100.0, 102.0), minRecords = 5)
      check.check().status shouldBe HealthStatus.Healthy  // grace
      check.check().status shouldBe HealthStatus.Healthy  // diff=100
      check.check().status shouldBe HealthStatus.Unhealthy // diff=2
   }

   test("lastTotal is updated between checks so diff is relative to previous call") {
      // Totals: 0 → 10 → 12 → 20. With minRecords=5: diffs are 10, 2, 8.
      val check = KafkaConsumerRecordsConsumedHealthCheck(consumer(0.0, 10.0, 12.0, 20.0), minRecords = 5)
      check.check().status shouldBe HealthStatus.Healthy   // total=0, grace
      check.check().status shouldBe HealthStatus.Healthy   // diff=10 >= 5
      check.check().status shouldBe HealthStatus.Unhealthy // diff=2  <  5
      check.check().status shouldBe HealthStatus.Healthy   // diff=8 >= 5
   }
})
