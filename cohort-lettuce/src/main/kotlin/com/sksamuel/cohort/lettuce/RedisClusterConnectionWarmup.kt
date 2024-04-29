//package com.sksamuel.cohort.lettuce
//
//import com.sksamuel.cohort.Warmup
//import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
//import kotlinx.coroutines.future.await
//import kotlin.random.Random
//
///**
// * A Cohort [Warmup] that uses a supplied Lettuce [StatefulRedisClusterConnection]
// * to execute commands against a redis cluster.
// *
// * By default, the [eval] function will set elements with a 1 second TTL under random keys with
// * the prefix "cohort_warmup". Any chain of commands can be used by providing a custom [eval] function.
// */
//class RedisClusterConnectionWarmup<K, V>(
//   private val conn: StatefulRedisClusterConnection<K, V>,
//   private val eval: suspend (StatefulRedisClusterConnection<K, V>) -> Unit,
//) : Warmup {
//
//   companion object {
//      operator fun invoke(
//         conn: StatefulRedisClusterConnection<String, *>
//      ): RedisClusterConnectionWarmup<String, *> {
//         return RedisClusterConnectionWarmup(conn) {
//            val key = "cohort_warmup_" + Random.nextInt()
//            it.async().incr(key).await()
//            it.async().expire(key, 1).await()
//            it.async().get(key).await()
//         }
//      }
//   }
//
//   override val name: String = "redis_cluster_connection_warmup"
//
//   override suspend fun warm(iteration: Int) {
//      eval(conn)
//   }
//}
