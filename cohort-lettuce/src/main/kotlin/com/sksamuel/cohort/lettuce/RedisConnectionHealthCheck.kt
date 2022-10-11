package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance.
 *
 * @param command a command to execute against the redis instance. Defaults to retrieving a random key.
 */
class RedisConnectionHealthCheck<K, V>(
   private val conn: StatefulRedisConnection<K, V>,
   private val command: suspend (StatefulRedisConnection<K, V>) -> Unit,
) : HealthCheck {

   companion object {

      operator fun <K> invoke(conn: StatefulRedisConnection<K, *>, genkey: () -> K): RedisConnectionHealthCheck<K, *> {
         return RedisConnectionHealthCheck(conn) { it.async().get(genkey()).await() }
      }

      operator fun invoke(conn: StatefulRedisConnection<String, *>): RedisConnectionHealthCheck<String, *> {
         return RedisConnectionHealthCheck(conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }


   override val name: String = "redis"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         command(conn)
         HealthCheckResult.Healthy("Redis command successful")
      }.getOrElse { HealthCheckResult.Unhealthy("Redis command failure", it) }
   }
}
