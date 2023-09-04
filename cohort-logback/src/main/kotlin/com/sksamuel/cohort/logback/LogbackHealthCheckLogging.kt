package com.sksamuel.cohort.logback

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.Subscriber
import org.slf4j.LoggerFactory

class LogbackHealthCheckLogging : Subscriber {

   private val logger = LoggerFactory.getLogger(LogbackHealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      logger.info("HealthCheck $name: $result")
   }
}
