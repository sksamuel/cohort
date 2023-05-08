package com.sksamuel.cohort.redis

import com.sksamuel.cohort.WarmupHealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import kotlin.random.Random

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
