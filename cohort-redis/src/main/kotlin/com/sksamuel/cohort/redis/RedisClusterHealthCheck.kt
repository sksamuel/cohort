package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisCluster

data class HostPort(val host: String, val port: Int)

/**
 * A [HealthCheck] that checks that a connection can be made to a redis cluster.
 *
 * @param command an optional command to execute against the redis cluster. Defaults to listing cluster nodes.
 *                Must return true if the command was successful.
 */
class RedisClusterHealthCheck(
  private val hostsAndPorts: Set<HostPort>,
  private val username: String?,
  private val password: String?,
  private val tls: Boolean,
  private val command: (JedisCluster) -> Boolean = { it.clusterNodes.isNotEmpty() },
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return withContext(Dispatchers.IO) {
      runCatching {
        val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
        val jedis = JedisCluster(hostsAndPorts.map { HostAndPort(it.host, it.port) }.toSet(), config)
        val success = jedis.use { command(it) }
        if (success) HealthCheckResult.Healthy("Connected to redis")
        else HealthCheckResult.Unhealthy("Redis health check command failed", null)
      }.getOrElse {
        HealthCheckResult.Unhealthy("Could not connect to redis cluster ${hostsAndPorts.joinToString(", ")}", it)
      }
    }
  }
}
