package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class RabbitConnectionHealthCheck(
   private val factory: ConnectionFactory,
   override val name: String = "rabbit_connection",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      return try {
         withTimeout(5.seconds) {
            runInterruptible(Dispatchers.IO) {
               factory.newConnection().use {
                  HealthCheckResult.healthy("Connected to rabbit instance")
               }
            }
         }
      } catch (t: TimeoutCancellationException) {
         HealthCheckResult.unhealthy("Could not connect to rabbit instance (timed out)", t)
      } catch (c: CancellationException) {
         throw c
      } catch (t: Throwable) {
         HealthCheckResult.unhealthy("Could not connect to rabbit instance", t)
      }
   }
}
