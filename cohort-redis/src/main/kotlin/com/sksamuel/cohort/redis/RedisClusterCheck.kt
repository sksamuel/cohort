package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import redis.clients.jedis.HostAndPort
import redis.clients.jedis.JedisClientConfig
import redis.clients.jedis.JedisCluster

/**
 * A [Check] that checks that a connection can be made to a redis cluster.
 *
 * @param config provides access details to the cluster.
 */
class RedisClusterCheck(
  private val hostsAndPorts: Set<HostAndPort>,
  private val config: JedisClientConfig,
) : Check {

  override suspend fun check(): CheckResult {
    return runCatching {
      val jedis = JedisCluster(hostsAndPorts, config)
      when (val nodes = jedis.clusterNodes.size) {
        0 -> CheckResult.Unhealthy("Connected to redis cluster but 0 nodes are available", null)
        else -> CheckResult.Healthy("Connected to redis cluster and $nodes nodes are available")
      }
    }.getOrElse {
      CheckResult.Unhealthy("Could not connect to redis cluster at ${hostsAndPorts.joinToString(", ")}", it)
    }
  }
}
