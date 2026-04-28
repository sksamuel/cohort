package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.Metric
import org.apache.kafka.common.MetricName

class KafkaConsumerTimeBetweenPollHealthCheckTest : FunSpec({

   fun consumer(timeBetweenPollMs: Double): Consumer<*, *> {
      val consumer = mockk<Consumer<*, *>>()
      val metricName = MetricName("time-between-poll-avg", "consumer-fetch-manager-metrics", "", emptyMap())
      val metric = mockk<Metric>()
      every { metric.metricName() } returns metricName
      every { metric.metricValue() } returns timeBetweenPollMs
      every { consumer.metrics() } returns mapOf(metricName to metric)
      return consumer
   }

   test("returns healthy when time is zero (consumer not yet polling)") {
      KafkaConsumerTimeBetweenPollHealthCheck(consumer(0.0), maxThreshold = 5000L)
         .check().status shouldBe HealthStatus.Healthy
   }

   test("returns healthy when time between polls is well below threshold") {
      // Polling every 100ms is well under the 5000ms stall threshold
      KafkaConsumerTimeBetweenPollHealthCheck(consumer(100.0), maxThreshold = 5000L)
         .check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when time between polls exceeds threshold") {
      // 10 seconds between polls with a 5000ms threshold — consumer is stalled
      KafkaConsumerTimeBetweenPollHealthCheck(consumer(10000.0), maxThreshold = 5000L)
         .check().status shouldBe HealthStatus.Unhealthy
   }

   test("returns healthy when time between polls exactly equals threshold") {
      KafkaConsumerTimeBetweenPollHealthCheck(consumer(5000.0), maxThreshold = 5000L)
         .check().status shouldBe HealthStatus.Healthy
   }
})
