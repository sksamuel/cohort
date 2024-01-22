package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.Consumer
import kotlin.math.roundToLong

/**
 * A Cohort [HealthCheck] that checks that the consumer is consuming a minimum number of
 * records between invocations, across any topics this consumer is subscribed to.
 *
 * This metric does not begin to return unhealthy until the total is above zero. A total of zero,
 * which means no records were consumed, returns healthy. This allows consumers to take time
 * to subscribe and begin consuming.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaConsumerRecordsConsumedHealthCheck(
   consumer: Consumer<*, *>,
   private val minRecords: Int,
   override val name: String = "kafka_consumer_records_consumed",
) : AbstractKafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(minRecords > 0) { "The minimum thresold is > 0" }
   }

   private val metricName = "records-consumed-total"
   private var lastTotal = 0L

   override suspend fun check(): HealthCheckResult {
      return metric(metricName).map { metric ->
         val total = metric.metricValue().toString().toDoubleOrNull()?.roundToLong() ?: 0L
         val diff = lastTotal - total
         val msg = "Kafka consumer $metricName total=$total diff=$diff [minRecords $minRecords]"
         return when {
            total == 0L -> HealthCheckResult.healthy(msg)
            total < minRecords -> HealthCheckResult.unhealthy(msg, null)
            else -> HealthCheckResult.healthy(msg)
         }
      }.fold({ it }, { it })
   }
}
