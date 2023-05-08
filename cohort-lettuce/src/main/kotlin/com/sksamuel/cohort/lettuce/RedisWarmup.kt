package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.WarmupHealthCheck
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

class RedisWarmup<K, V>(
   override val iterations: Int,
   private val conn: StatefulRedisConnection<K, V>,
   private val command: suspend (StatefulRedisConnection<K, V>) -> Unit,
) : WarmupHealthCheck() {

   companion object {

      operator fun <K> invoke(
         iterations: Int,
         conn: StatefulRedisConnection<K, *>,
         genkey: () -> K
      ): RedisWarmup<K, *> {
         return RedisWarmup(iterations, conn) { it.async().get(genkey()).await() }
      }

      operator fun invoke(iterations: Int, conn: StatefulRedisConnection<String, *>): RedisWarmup<String, *> {
         return RedisWarmup(iterations, conn) { it.async().get(Random.nextInt().toString()).await() }
      }
   }

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      command(conn)
   }
}
