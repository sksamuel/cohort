package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer

/**
 * A [HealthCheck] that checks that a kafka consumer is consuming a minimum number of messages.
 *
 * This check can be useful to detect stalled consumers.
 *
 * This check reports healthy if the records consumed between invocations is >= [min].
 * For example, when you register this check, if you specify a min value of 100, then at least
 * 100 records must be consumed during the interval you specify when you registered the check.
 *
 * @param healthyUntilFirstRecord if true, then this healthcheck is healthy while no records have yet been received.
 * This allows a consumer pod to go healthy first, and only report unhealthy if later throughput stalls.
 */
class KafkaConsumerCountHealthCheck(
   private val consumer: KafkaConsumer<*, *>,
   private val min: Int,
   private val healthyUntilFirstRecord: Boolean = false,
) : HealthCheck {

   private val metricName = "records-consumed-total"

   override val name: String = "kafka_consumer_count"

   private var lastTotal: Long = -1

   override suspend fun check(): HealthCheckResult {

      val metric = consumer.metrics().values.firstOrNull { it.metricName().name() == metricName }
         ?: return HealthCheckResult.unhealthy("Could not locate kafka metric '${metricName}'", null)

      val total = metric.metricValue().toString().toLongOrNull() ?: 0L

      // first time
      return if (lastTotal == -1L || (healthyUntilFirstRecord && total == 0L)) {
         lastTotal = total
         HealthCheckResult.healthy("Kafka consumer count initial scan")
      } else {
         val diff = total - lastTotal
         lastTotal = total
         val msg = "Kafka consumer $metricName since last scan $diff [min threshold $min]"
         if (diff < min) {
            HealthCheckResult.unhealthy(msg, null)
         } else {
            HealthCheckResult.healthy(msg)
         }
      }
   }
}
