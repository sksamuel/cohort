package com.sksamuel.cohort.redis

import com.sksamuel.cohort.WarmupHealthCheck
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class RedisClusterWarmupHealthCheck(
   client: RedisClusterClient,
   override val iterations: Int = 2500,
   override val interval: Duration = 2.milliseconds,
   private val command: suspend (StatefulRedisClusterConnection<String, String>) -> Unit = {
      it.async().get(Random.nextInt().toString()).await()
   },
) : WarmupHealthCheck() {

   override val name: String = "redis_warmup"

   private val connection = client.connect()

   override suspend fun warmup() {
      command(connection)
   }
}
