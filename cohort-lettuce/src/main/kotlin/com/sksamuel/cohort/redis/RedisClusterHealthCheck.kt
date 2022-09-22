package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.future.await
import kotlin.random.Random

data class HostPort(val host: String, val port: Int)

/**
 * A [HealthCheck] that checks that a connection can be made to a redis cluster.
 *
 * @param command a command to execute against the redis cluster. Defaults to retrieving a random key.
 */
class RedisClusterHealthCheck(
   private val client: RedisClusterClient,
   private val command: suspend (RedisClusterClient) -> HealthCheckResult = { it ->
      runCatching {
         it.connect().use { conn ->
            conn.async().get(Random.nextInt().toString()).await()
            HealthCheckResult.Healthy("Connected to redis cluster")
         }
      }.getOrElse { HealthCheckResult.Unhealthy("Error connecting to redis cluster", it) }
   },
) : HealthCheck {

   override val name: String = "redis_cluster"

   override suspend fun check(): HealthCheckResult {
      return command(client)
   }
}
