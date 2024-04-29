//package com.sksamuel.cohort.redis
//
//import com.sksamuel.cohort.Warmup
//import com.sksamuel.cohort.WarmupHealthCheck
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import redis.clients.jedis.JedisCluster
//import kotlin.random.Random
//
//class JedisClusterWarmup(
//   private val jedis: JedisCluster,
//   private val command: (JedisCluster, Int) -> Unit = { conn, _ -> conn.get(Random.nextInt().toString()) },
//) : Warmup {
//
//   override val name: String = "redis_cluster_connection_warmup"
//
//   override suspend fun warm(iteration: Int) {
//      withContext(Dispatchers.IO) { command.invoke(jedis, iteration) }
//   }
//}
