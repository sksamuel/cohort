package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.Listener
import com.sksamuel.cohort.Subscriber
import org.apache.logging.log4j.LogManager
import kotlin.time.Duration

/**
 * Logs each time a health check is invoked.
 *
 * @param logHealthyStatus if true, then log both healthy and unhealthy. If false, only log unhealthy statuses.
 */
@Deprecated("Use Log4j2HealthCheckLoggingListener")
class Log4j2HealthCheckLogging(private val logHealthyStatus: Boolean = true) : Subscriber {

   private val logger = LogManager.getLogger(Log4j2HealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus)
         logger.info("HealthCheck $name: $result")
   }
}

/**
 * Logs each time a health check is invoked.
 *
 * @param logHealthyStatus if true, then log both healthy and unhealthy. If false, only log unhealthy statuses.
 */
class Log4j2HealthCheckLoggingListener(private val logHealthyStatus: Boolean) : Listener {

   private val logger = LogManager.getLogger(Log4j2HealthCheckLoggingListener::class.java)

   override fun invoked(name: String, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus) {
         val msg = "Healthcheck ${result.status.name.padEnd(10, ' ')} '${name.padEnd(50, ' ')}': ${result.message}"
         // Attach the cause as the throwable arg when the check is unhealthy, so the full
         // stack trace is logged. Without this, the listener-based replacement drops
         // diagnostic information that the deprecated subscriber-based class included.
         val cause = result.cause
         if (cause != null && result.status == HealthStatus.Unhealthy) logger.warn(msg, cause)
         else logger.info(msg)
      }
   }

   override fun registered(name: String, initialDelay: Duration, checkInterval: Duration) {
      logger.info("Healthcheck registered: '${name}'")
   }
}
