package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisClientConfig

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance.
 *
 * @param config provides access details to the cluster.
 */
class RedisHealthCheck(
  private val hostsAndPort: HostAndPort,
  private val config: JedisClientConfig,
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return withContext(Dispatchers.IO) {
      runCatching {
        val jedis = Jedis(hostsAndPort, config)
        jedis.connection.use { it.ping() }
        HealthCheckResult.Healthy("Connected to redis cluster")
      }.getOrElse {
        HealthCheckResult.Unhealthy("Could not connect to redis at $hostsAndPort", it)
      }
    }
  }
}
