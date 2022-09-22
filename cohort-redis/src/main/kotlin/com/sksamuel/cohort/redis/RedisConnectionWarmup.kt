package com.sksamuel.cohort.redis

import com.sksamuel.cohort.WarmupHealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RedisConnectionWarmup(
   private val jedis: Jedis,
   override val iterations: Int = 2500,
   override val interval: Duration = 2.milliseconds,
   private val command: (Jedis) -> Unit = { it.get(Random.nextInt().toString()) }
) : WarmupHealthCheck() {

   override val name: String = "redis_warmup"

   override suspend fun warmup() {
      withContext(Dispatchers.IO) { command(jedis) }
   }
}
