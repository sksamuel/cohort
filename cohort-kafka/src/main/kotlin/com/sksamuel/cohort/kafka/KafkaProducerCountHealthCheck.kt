package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.producer.Producer

/**
 * A [HealthCheck] that checks that a kafka producer is producing enough records.
 *
 * This check can be useful to detect stalled producers.
 *
 * This check reports healthy if the minimum number of records sent since the last scan is >= [min].
 */
class KafkaProducerCountHealthCheck(
   private val producer: Producer<*, *>,
   private val min: Int,
) : HealthCheck {

   private val metricName = "record-send-total"

   override val name: String = "kafka_producer_rate"

   private var lastTotal: Long = -1

   override suspend fun check(): HealthCheckResult {

      val metric = producer.metrics().values.firstOrNull { it.metricName().name() == metricName }
         ?: return HealthCheckResult.unhealthy("Could not locate kafka metric '${metricName}'", null)

      val total = metric.metricValue().toString().toLongOrNull() ?: 0

      // first time
      return if (lastTotal == -1L) {
         lastTotal = total
         HealthCheckResult.healthy("Kafka producer count initial scan")
      } else {
         val diff = total - lastTotal
         lastTotal = total
         val msg = "Kafka producer $metricName since last scan $diff [min threshold $min]"
         if (diff < min) {
            HealthCheckResult.unhealthy(msg, null)
         } else {
            HealthCheckResult.healthy(msg)
         }
      }
   }
}
