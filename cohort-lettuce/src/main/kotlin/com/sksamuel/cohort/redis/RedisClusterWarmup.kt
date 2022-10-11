package com.sksamuel.cohort.redis

import com.sksamuel.cohort.Warmup
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import kotlinx.coroutines.future.await
import kotlin.random.Random

class RedisClusterWarmup(
   client: RedisClusterClient,
   private val command: suspend (StatefulRedisClusterConnection<String, String>) -> Unit = {
      it.async().get(Random.nextInt().toString()).await()
   },
) : Warmup {

   override val name: String = "redis_warmup"

   private val connection = client.connect()

   override suspend fun warm(iteration: Int) {
      command(connection)
   }
}
