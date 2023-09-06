package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.Subscriber
import org.apache.logging.log4j.LogManager

/**
 * Logs each time a health check is invoked.
 *
 * @param logHealthyStatus if true, then log both healthy and unhealthy. If false, only log unhealthy statuses.
 */
class Log4j2HealthCheckLogging(private val logHealthyStatus: Boolean = true) : Subscriber {

   private val logger = LogManager.getLogger(Log4j2HealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      if (result.status == HealthStatus.Unhealthy || logHealthyStatus)
         logger.info("HealthCheck $name: $result")
   }
}
