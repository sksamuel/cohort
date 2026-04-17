package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.Consumer
import kotlin.math.roundToLong

/**
 * A Cohort [HealthCheck] that checks that the average time between polls does not exceed a given maximum.
 * This metric is measured in milliseconds.
 *
 * This metric does not begin to return unhealthy until the time is above zero. A time of zero,
 * which means the consumer has not yet polled, returns healthy. This allows consumers to take time
 * to subscribe and begin consuming.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaConsumerTimeBetweenPollHealthCheck(
   consumer: Consumer<*, *>,
   private val maxThreshold: Long,
   override val name: String = "kafka_consumer_time_between_poll_avg",
) : AbstractKafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(maxThreshold > 0) { "The maximum threshold must be > 0" }
   }

   private val metricName = "time-between-poll-avg"

   override suspend fun check(): HealthCheckResult {
      return metric(metricName).map { metric ->
         val rate = runCatching { metric.metricValue().toString().toDouble().roundToLong() }.getOrElse { 0L }
         val msg = "Kafka consumer time-between-poll-avg ${rate}ms [maxThreshold ${maxThreshold}ms]"
         return when {
            rate == 0L -> HealthCheckResult.healthy(msg)
            rate > maxThreshold -> HealthCheckResult.unhealthy(msg, null)
            else -> HealthCheckResult.healthy(msg)
         }
      }.fold({ it }, { it })
   }
}
