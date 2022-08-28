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
  private val command: (JedisCluster) -> HealthCheckResult = {
    val nodes = it.clusterNodes.size
    if (nodes > 0) {
      HealthCheckResult.Healthy("Connected to redis cluster with $nodes nodes")
    } else {
      HealthCheckResult.Unhealthy("Connected to redis cluster but zero nodes detected", null)
    }
  },
) : HealthCheck {

  override val name: String = "redis_cluster"

  override suspend fun check(): HealthCheckResult {
    return withContext(Dispatchers.IO) {
      runCatching {
        val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
        val jedis = JedisCluster(hostsAndPorts.map { HostAndPort(it.host, it.port) }.toSet(), config)
        jedis.use { command(it) }
      }.getOrElse {
        HealthCheckResult.Unhealthy("Could not connect to redis cluster ${hostsAndPorts.joinToString(", ")}", it)
      }
    }
  }
}
