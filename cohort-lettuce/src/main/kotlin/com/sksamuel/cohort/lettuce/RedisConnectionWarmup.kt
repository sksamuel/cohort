package com.sksamuel.cohort.lettuce

import com.sksamuel.cohort.Warmup
import io.lettuce.core.api.StatefulRedisConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

/**
 * A Redis [Warmup] that uses a supplied Lettuce [StatefulRedisConnection]
 * to execute commands.
 *
 * By default, the [eval] function will set elements with a 1 second TTL under random keys with
 * the prefix "cohort_warmup". Any chain of commands can be used by providing a custom [eval] function.
 */
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
            it.async().expire(key, 1).await()
            it.async().get(key).await()
         }
      }
   }

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      command(conn)
   }
}
