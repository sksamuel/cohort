//package com.sksamuel.cohort.db
//
//import com.sksamuel.cohort.WarmupHealthCheck
//import javax.sql.DataSource
//import kotlin.time.Duration
//import kotlin.time.Duration.Companion.seconds
//
///**
// * A Cohort [WarmupHealthCheck] that warms a [DataSource] by executing a query.
// *
// * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
// * returned is open and usable.
// */
//@Deprecated("Use DataSourceConnectionWarmup")
//class DataSourceWarmup(
//   override val iterations: Int,
//   private val ds: DataSource,
//   private val query: String,
//   private val timeout: Duration = 1.seconds,
//) : WarmupHealthCheck() {
//
//   override val name: String = "datasource_warmup"
//
//   override suspend fun warm(iteration: Int) {
//      ds.connection.use { conn ->
//         conn.isValid(timeout.inWholeSeconds.toInt())
//         conn.createStatement().use { it.execute(query) }
//      }
//   }
//}
//
