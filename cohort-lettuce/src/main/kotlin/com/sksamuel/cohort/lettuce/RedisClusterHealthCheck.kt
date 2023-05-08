package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

/**
 * A [HealthCheck] that checks that a connection can be made to a redis instance.
 *
 * @param command a command to execute against the redis instance. Defaults to retrieving a random key.
 */
class RedisClusterHealthCheck<K, V>(
   private val conn: StatefulRedisClusterConnection<K, V>,
   private val command: suspend (StatefulRedisClusterConnection<K, V>) -> Unit,
) : HealthCheck {

   companion object {

      operator fun <K> invoke(conn: StatefulRedisClusterConnection<K, *>, genkey: () -> K): RedisClusterHealthCheck<K, *> {
         return RedisClusterHealthCheck(conn) { it.async().get(genkey()).await() }
      }

      operator fun invoke(conn: StatefulRedisClusterConnection<String, *>): RedisClusterHealthCheck<String, *> {
         return RedisClusterHealthCheck(conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }


   override val name: String = "redis_cluster"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         command(conn)
         HealthCheckResult.healthy("Redis command successful")
      }.getOrElse { HealthCheckResult.unhealthy("Redis command failure", it) }
   }
}
