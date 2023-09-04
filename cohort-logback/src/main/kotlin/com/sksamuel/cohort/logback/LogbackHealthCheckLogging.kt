package com.sksamuel.cohort.logback

import com.sksamuel.cohort.HealthCheckRegistry
import org.slf4j.LoggerFactory

class LogbackHealthCheckLogging {

   private val logger = LoggerFactory.getLogger(LogbackHealthCheckLogging::class.java)

   fun bindTo(registry: HealthCheckRegistry) {
      registry.addSubscriber { name, _, result ->
         logger.info("HealthCheck $name: $result")
      }
   }
}
