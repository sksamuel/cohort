//package com.sksamuel.cohort.lettuce
//
//import com.sksamuel.cohort.WarmupHealthCheck
//import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
//import kotlinx.coroutines.future.await
//import kotlin.random.Random
//
///**
// * A Redis [WarmupHealthCheck] that uses a supplied Lettuce [StatefulRedisClusterConnection]
// * to execute commands.
// *
// * By default, the [eval] function will set elements with a 1 second TTL under random keys with
// * the prefix "cohort_warmup". Any chain of commands can be used by providing a custom [eval] function.
// */
//@Deprecated("Use RedisClusterConnectionWarmup")
//class RedisClusterWarmup<K, V>(
//   private val conn: StatefulRedisClusterConnection<K, V>,
//   override val iterations: Int,
//   private val eval: suspend (StatefulRedisClusterConnection<K, V>) -> Unit,
//) : WarmupHealthCheck() {
//
//   companion object {
//
//      @Deprecated(
//         "Provide a custom eval function or use the default", ReplaceWith(
//            "RedisClusterWarmup(conn, iterations) { it.async().get(genkey()).await() }",
//            "com.sksamuel.cohort.lettuce.RedisClusterWarmup",
//            "kotlinx.coroutines.future.await"
//         )
//      )
//      operator fun <K> invoke(
//         iterations: Int,
//         conn: StatefulRedisClusterConnection<K, *>,
//         genkey: () -> K
//      ): RedisClusterWarmup<K, *> {
//         return RedisClusterWarmup(conn, iterations) { it.async().get(genkey()).await() }
//      }
//
//      operator fun invoke(
//         iterations: Int,
//         conn: StatefulRedisClusterConnection<String, *>
//      ): RedisClusterWarmup<String, *> {
//         return RedisClusterWarmup(conn, iterations) {
//            val key = "cohort_warmup_" + Random.nextInt()
//            it.async().incr(key).await()
//            it.async().expire(key, 1).await()
//            it.async().get(key).await()
//         }
//      }
//   }
//
//   override val name: String = "redis_warmup"
//
//   override suspend fun warm(iteration: Int) {
//      eval(conn)
//   }
//}
//
