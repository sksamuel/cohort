package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

/**
 * A [HealthCheck] that checks that a connection can be made to a redis cluster using
 * the supplied lettuce connection.
 *
 * @param command a command to execute against the redis instance.
 *                Defaults to retrieving a random key, which may or may not exist.
 */
class RedisClusterHealthCheck<K, V>(
   private val conn: StatefulRedisClusterConnection<K, V>,
   override val name: String = "redis_cluster",
   private val command: suspend (StatefulRedisClusterConnection<K, V>) -> Unit,
) : HealthCheck {

   companion object {

      /**
       * Uses the supplied [conn] to retrieve a key from Redis, with the key generated by the [genkey] function.
       */
      operator fun <K> invoke(
         conn: StatefulRedisClusterConnection<K, *>,
         genkey: () -> K
      ): RedisClusterHealthCheck<K, *> {
         return RedisClusterHealthCheck(conn) { it.async().get(genkey()).await() }
      }

      /**
       * Uses the supplied [conn] to retrieve a key from Redis, with the key randomly generated.
       */
      operator fun invoke(conn: StatefulRedisClusterConnection<String, *>): RedisClusterHealthCheck<String, *> {
         return RedisClusterHealthCheck(conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         command(conn)
         HealthCheckResult.healthy("Redis command successful")
      }.getOrElse { HealthCheckResult.unhealthy("Redis command failure", it) }
   }
}
