package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Warmup
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RedisClusterWarmup(
   client: RedisClusterClient,
   override val iterations: Int = 1000,
   override val interval: Duration = 10.milliseconds,
   private val command: suspend (StatefulRedisClusterConnection<String, String>) -> Unit = {
      it.async().get(Random.nextInt().toString()).await()
   },
) : Warmup() {

   override val name: String = "redis_warmup"

   private val connection = client.connect()

   override suspend fun warmup() {
      command(connection)
   }
}
