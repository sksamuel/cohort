package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.Subscriber
import org.apache.logging.log4j.LogManager

class Log4j2HealthCheckLogging : Subscriber {

   private val logger = LogManager.getLogger(Log4j2HealthCheckLogging::class.java)

   override suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult) {
      logger.info("HealthCheck $name: $result")
   }
}
