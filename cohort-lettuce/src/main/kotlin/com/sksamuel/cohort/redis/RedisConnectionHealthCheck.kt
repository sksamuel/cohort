package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.lettuce.core.RedisClient
import kotlinx.coroutines.future.await
import kotlin.random.Random

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance.
 *
 * @param command a command to execute against the redis instance. Defaults to retrieving a random key.
 */
class RedisConnectionHealthCheck(
   private val client: RedisClient,
   private val command: suspend (RedisClient) -> HealthCheckResult = {
      runCatching {
         it.connect().use { conn ->
            conn.async().get(Random.nextInt().toString()).await()
            HealthCheckResult.Healthy("Connected to cluster")
         }
      }.getOrElse { HealthCheckResult.Unhealthy("Error connecting to cluster", it) }
   }
) : HealthCheck {

   override val name: String = "redis"

   override suspend fun check(): HealthCheckResult {
      return command(client)
   }
}
