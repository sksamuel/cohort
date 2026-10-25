package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.Consumer

/**
 * A Cohort [HealthCheck] that checks that the rate of consumption of records per second
 * is above a given minimum.
 *
 * This metric does not begin to return unhealthy until the rate is above zero. A rate of zero,
 * which means no records were consumed, returns healthy. This allows consumers to take time
 * to subscribe and begin consuming.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaConsumerRecordsConsumedRateHealthCheck(
   consumer: Consumer<*, *>,
   private val minThreshold: Double,
   override val name: String = "kafka_consumer_records_consumed_rate",
) : AbstractKafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(minThreshold > 0.0) { "The minimum threshold is > 0.0" }
   }

   private val metricName = "records-consumed-rate"

   override suspend fun check(): HealthCheckResult {
      return metric(metricName).map { metric ->
         // "NaN".toDoubleOrNull() returns Double.NaN (not null). All comparisons against NaN
         // are false, so the previous code fell through `rate == 0.0` and `rate < minThreshold`
         // to the healthy branch, silently masking a NaN rate emitted by Kafka during
         // reconfiguration or zero-window divisions. Treat NaN as unhealthy explicitly.
         val raw = metric.metricValue().toString().toDoubleOrNull()
         if (raw == null || raw.isNaN()) {
            return@map HealthCheckResult.unhealthy(
               "Kafka consumer records-consumed-rate is unavailable [$raw]",
               null,
            )
         }
         val msg = "Kafka consumer records-consumed-rate $raw [minThreshold $minThreshold]"
         when {
            raw == 0.0 -> HealthCheckResult.healthy(msg)
            raw < minThreshold -> HealthCheckResult.unhealthy(msg, null)
            else -> HealthCheckResult.healthy(msg)
         }
      }.fold({ it }, { it })
   }
}
