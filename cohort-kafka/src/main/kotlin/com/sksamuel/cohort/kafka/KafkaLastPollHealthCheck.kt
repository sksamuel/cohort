package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer
import kotlin.math.roundToInt
import kotlin.time.Duration

@Deprecated("use KafkaLastPollHealthCheck")
typealias KafkaConsumerLastPollTimeHealthCheck = KafkaLastPollHealthCheck

/**
 * A Cohort [HealthCheck] that checks that a kafka consumer made a call to poll,
 * regardless of whether the poll returned records or not, within the given [interval] period.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaLastPollHealthCheck(
   private val consumer: KafkaConsumer<*, *>,
   private val interval: Duration,
) : KafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(interval.inWholeSeconds > 0) { "The minimum resolution is 1 second" }
   }

   private val metricName = "last-poll-seconds-ago"

   override val name: String = "kafka_consumer_last_poll"

   override suspend fun check(): HealthCheckResult {
      return metric(metricName).map { metric ->
         val lastPollSecondsAgo = metric.metricValue().toString().toDoubleOrNull()?.roundToInt() ?: 0
         val msg = "Kafka consumer last polled $lastPollSecondsAgo [max ${interval.inWholeSeconds}]"
         return if (lastPollSecondsAgo > interval.inWholeSeconds)
            HealthCheckResult.unhealthy(msg, null)
         else
            HealthCheckResult.healthy(msg)
      }.fold({ it }, { it })
   }
}
