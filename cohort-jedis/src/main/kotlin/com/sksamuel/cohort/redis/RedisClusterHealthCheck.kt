package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
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
   private val jedis: JedisCluster,
   private val command: (JedisCluster) -> HealthCheckResult = {
      val nodes = it.clusterNodes.size
      if (nodes > 0) {
         HealthCheckResult.healthy("Connected to redis cluster with $nodes nodes")
      } else {
         HealthCheckResult.unhealthy("Connected to redis cluster but zero nodes detected", null)
      }
   },
   override val name: String = "redis_cluster",
) : HealthCheck {

   companion object {
      operator fun invoke(
         hostsAndPorts: Set<HostPort>,
         username: String?,
         password: String?,
         tls: Boolean,
         command: (JedisCluster) -> HealthCheckResult = {
            val nodes = it.clusterNodes.size
            if (nodes > 0) {
               HealthCheckResult.healthy("Connected to redis cluster with $nodes nodes")
            } else {
               HealthCheckResult.unhealthy("Connected to redis cluster but zero nodes detected", null)
            }
         }
      ): RedisClusterHealthCheck {
         val config = DefaultJedisClientConfig.builder().password(password).user(username).ssl(tls).build()
         val jedis = JedisCluster(hostsAndPorts.map { HostAndPort(it.host, it.port) }.toSet(), config)
         return RedisClusterHealthCheck(jedis, command)
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            jedis.use { command(it) }
         }.getOrElse {
            HealthCheckResult.unhealthy("Could not connect to redis cluster", it)
         }
      }
   }
}
