package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.ConsumerConfig

/**
 * A Cohort [HealthCheck] that checks for stalled consumers by using a [CountingConsumerInterceptor]
 * to track progress. A consumer must have consumed at least one record between invocations for this health
 * check to report healthy.
 *
 * For this healthcheck to work, each consumer to be monitored must be configured with a
 * CountingConsumerInterceptor in config. Eg,
 *
 * val props = Props()
 * props[ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG] = CountingConsumerInterceptor::class.java.name
 *
 * This class tracks counts for each consumer group.
 * if you do not want a consumer to be tracked, do not register the CountingConsumerInterceptor.
 *
 * Note: It is best to register this check with a reasonably long period between invocations to allow
 * for rebalances to occur. For example, if this was registered with 20 second intervals, it is quite likely
 * that during a deployment, a rebalance could take longer than 20 seconds, thus reporting unhealthy.
 *
 * This check can be useful to detect consumers which are making no progress.
 */
class KafkaConsumersHealthCheck : HealthCheck {

   private var previousCounts: Map<String, Long> = emptyMap()

   override suspend fun check(): HealthCheckResult {
      val counts = Counter.counts()

      // a consumer is considered stalled if the count is > 0 and == to the previous count
      val stalled = counts.filter {
         val previousCount = previousCounts[it.key] ?: 0L
         previousCount > 0L && previousCount == it.value
      }.keys

      previousCounts = counts

      return if (stalled.isEmpty()) {
         HealthCheckResult.healthy("All consumers are making progress")
      } else {
         HealthCheckResult.unhealthy("Kakfa consumers are stalled on: ${stalled.joinToString(",")}")
      }
   }
}
