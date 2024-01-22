package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class RabbitConnectionHealthCheck(
   private val factory: ConnectionFactory,
   override val name: String = "rabbit_connection",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         withTimeout(5.seconds) {
            runInterruptible(Dispatchers.IO) {
               factory.newConnection()
               HealthCheckResult.healthy("Connected to rabbit instance")
            }
         }
      }.getOrElse {
         HealthCheckResult.unhealthy("Could not connect to rabbit instance", it)
      }
   }
}
