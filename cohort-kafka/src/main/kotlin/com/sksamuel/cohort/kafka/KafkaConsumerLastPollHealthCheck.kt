package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.Consumer
import kotlin.math.roundToInt
import kotlin.time.Duration

/**
 * A Cohort [HealthCheck] that checks that a kafka consumer made a call to poll,
 * regardless of whether the poll returned records or not, within the given [interval] period,
 * across any topics this consumer is subscribed to.
 *
 * The granularity on the check is seconds, so specifying an [interval] less than 1 second
 * will always result in an error.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaConsumerLastPollHealthCheck(
   consumer: Consumer<*, *>,
   private val interval: Duration,
   override val name: String = "kafka_consumer_last_poll",
) : AbstractKafkaConsumerMetricHealthCheck(consumer) {

   init {
      require(interval.inWholeSeconds > 0) { "The minimum resolution is 1 second" }
   }

   private val metricName = "last-poll-seconds-ago"

   override suspend fun check(): HealthCheckResult {
      val metric = metricOrNull(metricName)
      val lastPollSecondsAgo = metric?.metricValue()?.toString()?.toDoubleOrNull()?.roundToInt() ?: 0
      val msg = "Kafka consumer last polled $lastPollSecondsAgo [max ${interval.inWholeSeconds}]"
      return if (lastPollSecondsAgo > interval.inWholeSeconds)
         HealthCheckResult.unhealthy(msg, null)
      else
         HealthCheckResult.healthy(msg)
   }
}
