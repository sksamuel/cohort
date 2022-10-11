package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.Warmup
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

class RedisClusterWarmup<K, V>(
   private val conn: StatefulRedisClusterConnection<K, V>,
   private val command: suspend (StatefulRedisClusterConnection<K, V>) -> Unit,
) : Warmup {

   companion object {

      operator fun <K> invoke(conn: StatefulRedisClusterConnection<K, *>, genkey: () -> K): RedisClusterWarmup<K, *> {
         return RedisClusterWarmup(conn) { it.async().get(genkey()).await() }
      }

      operator fun invoke(conn: StatefulRedisClusterConnection<String, *>): RedisClusterWarmup<String, *> {
         return RedisClusterWarmup(conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      command(conn)
   }
}
