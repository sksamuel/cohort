package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import redis.clients.jedis.Connection
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance and a command
 * invoked successfully.
 *
 * @param command an optional command to execute against the redis instance. Defaults to ping.
 */
class RedisConnectionHealthCheck(
   private val jedis: Jedis,
   private val command: (Connection) -> HealthCheckResult = {
      if (it.ping()) {
         HealthCheckResult.healthy("Connected to Redis")
      } else {
         HealthCheckResult.unhealthy("Ping to Redis failed", null)
      }
   },
   override val name: String = "redis",
) : HealthCheck {

   companion object {
      operator fun invoke(
         hostAndPort: HostPort,
         username: String?,
         password: String?,
         tls: Boolean,
         command: (Connection) -> HealthCheckResult = {
            if (it.ping()) {
               HealthCheckResult.healthy("Connected to Redis")
            } else {
               HealthCheckResult.unhealthy("Ping to Redis failed", null)
            }
         }
      ): RedisConnectionHealthCheck {
         val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
         val jedis = Jedis(HostAndPort(hostAndPort.host, hostAndPort.port), config)
         return RedisConnectionHealthCheck(jedis, command)
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         try {
            command(jedis.connection)
         } catch (c: CancellationException) {
            // runInterruptible converts thread interrupts to CancellationException. Don't
            // swallow it into a "Could not connect to Redis" result — let cancellation flow.
            throw c
         } catch (t: Throwable) {
            // InterruptedException surfaces here for non-coroutine interrupt paths; re-arm
            // the interrupt flag so the caller can observe it.
            if (t is InterruptedException) Thread.currentThread().interrupt()
            HealthCheckResult.unhealthy("Could not connect to Redis", t)
         }
      }
   }
}
