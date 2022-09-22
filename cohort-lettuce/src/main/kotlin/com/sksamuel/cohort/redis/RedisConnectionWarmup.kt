package com.sksamuel.cohort.redis

import com.sksamuel.cohort.WarmupHealthCheck
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RedisConnectionWarmup(
   client: RedisClient,
   override val iterations: Int = 2500,
   override val interval: Duration = 2.milliseconds,
   private val command: suspend (StatefulRedisConnection<String, String>) -> Unit = {
      it.sync().get(Random.nextInt().toString())
   }
) : WarmupHealthCheck() {

   override val name: String = "redis_warmup"

   private val conn = client.connect()

   override suspend fun warmup() {
      command(conn)
   }
}
