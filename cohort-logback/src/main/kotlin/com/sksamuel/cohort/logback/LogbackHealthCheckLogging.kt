package com.sksamuel.cohort.logback

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.Listener
import com.sksamuel.cohort.Subscriber
import org.slf4j.LoggerFactory
import kotlin.time.Duration

/**
 * Logs each time a health check is invoked.
 *
 * @param logHealthyStatus if true, then log both healthy and unhealthy. If false, only log unhealthy statuses.
 */
@Deprecated("Use LogbackHealthCheckLoggingListener")
class LogbackHealthCheckLogging(private val logHealthyStatus: Boolean = true) : Subscriber {

   private val logger = LoggerFactory.getLogger(LogbackHealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus)
         logger.info("HealthCheck $name: $result")
   }
}

class LogbackHealthCheckLoggingListener(private val logHealthyStatus: Boolean) : Listener {

   private val logger = LoggerFactory.getLogger(LogbackHealthCheckLoggingListener::class.java)

   override fun invoked(name: String, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus)
         logger.info("Healthcheck ${result.status.name.padEnd(10, ' ')} '${name.padEnd(50, ' ')}': ${result.message}")
   }

   override fun registered(name: String, initialDelay: Duration, checkInterval: Duration) {
      logger.info("Healthcheck registered: '${name}'")
   }
}
