package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.CheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisClientConfig
import redis.clients.jedis.JedisCluster

/**
 * A [HealthCheck] that checks that a connection can be made to a redis cluster.
 *
 * @param config provides access details to the cluster.
 */
class RedisClusterHealthCheck(
  private val hostsAndPorts: Set<HostAndPort>,
  private val config: JedisClientConfig,
) : HealthCheck {

  override suspend fun check(): CheckResult {
    return withContext(Dispatchers.IO) {
      runCatching {
        val jedis = JedisCluster(hostsAndPorts, config)
        jedis.use {
          when (val nodes = it.clusterNodes.size) {
            0 -> CheckResult.Unhealthy("Connected to redis cluster but 0 nodes are available", null)
            else -> CheckResult.Healthy("Connected to redis cluster and $nodes nodes are available")
          }
        }
      }.getOrElse {
        CheckResult.Unhealthy("Could not connect to redis cluster at ${hostsAndPorts.joinToString(", ")}", it)
      }
    }
  }
}
