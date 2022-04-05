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
 */
class RedisClusterHealthCheck(
  private val hostsAndPorts: Set<HostPort>,
  private val username: String?,
  private val password: String?,
  private val tls: Boolean,
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return withContext(Dispatchers.IO) {
      runCatching {
        val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
        val jedis = JedisCluster(hostsAndPorts.map { HostAndPort(it.host, it.port) }.toSet(), config)
        jedis.use {
          when (val nodes = it.clusterNodes.size) {
            0 -> HealthCheckResult.Unhealthy("Connected to redis cluster but 0 nodes are available", null)
            else -> HealthCheckResult.Healthy("Connected to redis cluster and $nodes nodes are available")
          }
        }
      }.getOrElse {
        HealthCheckResult.Unhealthy("Could not connect to redis cluster at ${hostsAndPorts.joinToString(", ")}", it)
      }
    }
  }
}
