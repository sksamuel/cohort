package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Warmup
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import kotlin.random.Random

class RedisConnectionWarmup(
   client: RedisClient,
   private val command: suspend (StatefulRedisConnection<String, String>) -> Unit = {
      it.sync().get(Random.nextInt().toString())
   }
) : Warmup {

   override val name: String = "redis_warmup"

   private val conn = client.connect()

   override suspend fun warm(iteration: Int) {
      command(conn)
   }
}
