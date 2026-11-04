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

/**
 * A Cohort [HealthCheck] for RabbitMQ queue access. This health check will perform a
 * passive queue declaration which simply checks that the queue is reachable and exists.
 * It will not create the queue.
 *
 */
class RabbitQueueHealthCheck(
   private val factory: ConnectionFactory,
   private val queue: String,
   override val name: String = "rabbit_queue",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      return try {
         withTimeout(5.seconds) {
            runInterruptible(Dispatchers.IO) {
               factory.newConnection().use { conn ->
                  conn.createChannel().use { channel ->
                     channel.queueDeclarePassive(queue)
                     HealthCheckResult.healthy("Confirmed connection to RabbitMQ queue $queue")
                  }
               }
            }
         }
      } catch (t: TimeoutCancellationException) {
         HealthCheckResult.unhealthy("Could not connect to RabbitMQ queue $queue (timed out)", t)
      } catch (c: CancellationException) {
         throw c
      } catch (t: Throwable) {
         HealthCheckResult.unhealthy("Could not connect to RabbitMQ, or queue does not exist $queue", t)
      }
   }
}
