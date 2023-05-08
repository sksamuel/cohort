package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
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
) : HealthCheck {

   override val name: String = "rabbit_queue"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         withTimeout(5.seconds) {
            runInterruptible(Dispatchers.IO) {
               val conn = factory.newConnection()
               val channel = conn.createChannel()
               channel.queueDeclarePassive(queue)
               HealthCheckResult.healthy("Confirmed connection to RabbitMQ queue $queue")
            }
         }
      }.getOrElse {
         HealthCheckResult.unhealthy("Could not connect to RabbitMQ, or queue does not exist $queue", it)
      }
   }
}
