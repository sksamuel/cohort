package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer
import kotlin.math.roundToInt
import kotlin.time.Duration

/**
 * A [HealthCheck] that checks that a kafka consumer last poll at most [interval] ago.
 *
 * This check can be useful to detect stalled consumers.
 */
class KafkaConsumerLastPollTimeHealthCheck(
  private val consumer: KafkaConsumer<*, *>,
  private val interval: Duration,
) : HealthCheck {

  private val metricName = "last-poll-seconds-ago"

  override suspend fun check(): HealthCheckResult {
    val metric = consumer.metrics().values.firstOrNull { it.metricName().name() == metricName }
      ?: return HealthCheckResult.Unhealthy("Could not locate kafka metric '${metricName}'", null)
    val lastPollSecondsAgo = metric.metricValue().toString().toDoubleOrNull()?.roundToInt() ?: 0
    val msg = "Kafka consumer last polled $lastPollSecondsAgo [max ${interval.inWholeSeconds}]"
    return if (lastPollSecondsAgo < interval.inWholeSeconds)
      HealthCheckResult.Unhealthy(msg, null)
    else
      HealthCheckResult.Healthy(msg)
  }
}
