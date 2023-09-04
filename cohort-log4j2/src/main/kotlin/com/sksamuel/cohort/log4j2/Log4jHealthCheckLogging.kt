package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.HealthCheckRegistry
import org.apache.logging.log4j.LogManager

class Log4jHealthCheckLogging {

   private val logger = LogManager.getLogger(Log4jHealthCheckLogging::class.java)

   fun bindTo(registry: HealthCheckRegistry) {
      registry.addSubscriber { name, _, result ->
         logger.info("HealthCheck $name: $result")
      }
   }
}
