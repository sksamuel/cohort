package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Warmup
import com.sksamuel.cohort.WarmupHealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import kotlin.random.Random

@Deprecated("Use JedisWarmup")
class RedisConnectionWarmup(
   private val jedis: Jedis,
   private val command: (Jedis) -> Unit = { it.get(Random.nextInt().toString()) },
   override val iterations: Int = 1000,
) : WarmupHealthCheck() {

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      withContext(Dispatchers.IO) { command(jedis) }
   }
}

class JedisWarmup(
   private val jedis: Jedis,
   private val command: (Jedis, Int) -> Unit = { conn, _ -> conn.get(Random.nextInt().toString()) },
) : Warmup {

   override val name: String = "redis_warmup"

   override suspend fun warm(iteration: Int) {
      withContext(Dispatchers.IO) { command(jedis, iteration) }
   }
}
