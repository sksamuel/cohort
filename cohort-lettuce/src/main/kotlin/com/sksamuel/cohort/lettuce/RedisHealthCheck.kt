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
class RedisHealthCheck<K, V>(
   private val conn: StatefulRedisConnection<K, V>,
   private val command: suspend (StatefulRedisConnection<K, V>) -> Unit,
) : HealthCheck {

   companion object {

      operator fun <K> invoke(conn: StatefulRedisConnection<K, *>, genkey: () -> K): RedisHealthCheck<K, *> {
         return RedisHealthCheck(conn) { it.async().get(genkey()).await() }
      }

      operator fun invoke(conn: StatefulRedisConnection<String, *>): RedisHealthCheck<String, *> {
         return RedisHealthCheck(conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }


   override val name: String = "redis"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         command(conn)
         HealthCheckResult.healthy("Redis command successful")
      }.getOrElse { HealthCheckResult.unhealthy("Redis command failure", it) }
   }
}
