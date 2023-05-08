package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import redis.clients.jedis.Connection
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance.
 *
 * @param command an optional command to execute against the redis instance. Defaults to ping.
 */
class RedisConnectionHealthCheck(
   private val jedis: Jedis,
   private val command: (Connection) -> HealthCheckResult = {
      if (it.ping()) {
         HealthCheckResult.healthy("Connected to redis cluster")
      } else {
         HealthCheckResult.healthy("Ping to redis cluster failed")
      }
   },
) : HealthCheck {

   companion object {
      operator fun invoke(
         hostAndPort: HostPort,
         username: String?,
         password: String?,
         tls: Boolean,
         command: (Connection) -> HealthCheckResult = {
            if (it.ping()) {
               HealthCheckResult.healthy("Connected to redis cluster")
            } else {
               HealthCheckResult.healthy("Ping to redis cluster failed")
            }
         }
      ): RedisConnectionHealthCheck {
         val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
         val jedis = Jedis(HostAndPort(hostAndPort.host, hostAndPort.port), config)
         return RedisConnectionHealthCheck(jedis, command)
      }
   }

   override val name: String = "redis"

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            jedis.connection.use { command(it) }
         }.getOrElse {
            HealthCheckResult.unhealthy("Could not connect to Redis", it)
         }
      }
   }
}
