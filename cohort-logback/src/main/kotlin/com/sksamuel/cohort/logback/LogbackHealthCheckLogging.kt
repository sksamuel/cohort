package com.sksamuel.cohort.logback

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.Subscriber
import org.slf4j.LoggerFactory

/**
 * Logs each time a health check is invoked.
 *
 * @param logHealthyStatus if true, then log both healthy and unhealthy. If false, only log unhealthy statuses.
 */
class LogbackHealthCheckLogging(private val logHealthyStatus: Boolean = true) : Subscriber {

   private val logger = LoggerFactory.getLogger(LogbackHealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus)
         logger.info("HealthCheck $name: $result")
   }
}
