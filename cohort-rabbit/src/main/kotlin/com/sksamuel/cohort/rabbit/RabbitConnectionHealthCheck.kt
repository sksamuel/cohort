package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class RabbitConnectionHealthCheck(private val factory: ConnectionFactory) : HealthCheck {

   override val name: String = "rabbit_connection"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         withTimeout(5.seconds) {
            runInterruptible(Dispatchers.IO) {
               factory.newConnection()
               HealthCheckResult.Healthy("Connected to rabbit instance")
            }
         }
      }.getOrElse {
         HealthCheckResult.Unhealthy("Could not connect to rabbit instance", it)
      }
   }
}
