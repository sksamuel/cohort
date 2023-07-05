package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.Warmup
import com.sksamuel.cohort.WarmupHealthCheck
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

class RedisConnectionWarmup<K, V>(
   private val conn: StatefulRedisConnection<K, V>,
   private val command: suspend (StatefulRedisConnection<K, V>) -> Unit,
) : Warmup {

   companion object {

      operator fun invoke(
         conn: StatefulRedisConnection<String, *>,
      ): RedisConnectionWarmup<String, *> {
         return RedisConnectionWarmup(conn) {
            val key = "cohort_warmup_" + Random.nextInt()
            it.async().incr(key).await()
            it.async().expire(key, 5).await()
            it.async().get(key).await()
         }
      }
   }

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      command(conn)
   }
}

@Deprecated("Use RedisConnectionWarmup")
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
