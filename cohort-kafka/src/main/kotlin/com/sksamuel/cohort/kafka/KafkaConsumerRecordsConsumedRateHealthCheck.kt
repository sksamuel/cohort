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
) : AbstractKafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(minThreshold > 0.0) { "The minimum thresold is > 0.0" }
   }

   private val metricName = "records-consumed-rate"

   override val name: String = "kafka_consumer_records_consumed_rate"

   override suspend fun check(): HealthCheckResult {
      return metric(metricName).map { metric ->
         val rate = metric.metricValue().toString().toDoubleOrNull() ?: 0.0
         val msg = "Kafka consumer records-consumed-rate $rate [minThreshold $minThreshold]"
         return when {
            rate == 0.0 -> HealthCheckResult.healthy(msg)
            rate < minThreshold -> HealthCheckResult.unhealthy(msg, null)
            else -> HealthCheckResult.healthy(msg)
         }
      }.fold({ it }, { it })
   }
}
