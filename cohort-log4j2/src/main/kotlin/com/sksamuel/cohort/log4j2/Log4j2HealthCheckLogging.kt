package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.HealthCheckRegistry
import org.apache.logging.log4j.LogManager

class Log4j2HealthCheckLogging {

   private val logger = LogManager.getLogger(Log4j2HealthCheckLogging::class.java)

   fun bindTo(registry: HealthCheckRegistry) {
      registry.addSubscriber { name, _, result ->
         logger.info("HealthCheck $name: $result")
      }
   }
}
