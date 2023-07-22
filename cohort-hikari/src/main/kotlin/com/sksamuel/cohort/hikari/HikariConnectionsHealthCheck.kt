package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [HealthCheck] that checks for the number of connections in a [HikariDataSource].
 *
 * This is useful to ensure a service has opened a required number of connections before being
 * considered healthy.
 *
 * The check is considered healthy if the total connection count is >= [minConnections].
 */
class HikariConnectionsHealthCheck(
   private val ds: HikariDataSource,
   private val minConnections: Int,
) : HealthCheck {

   override val name: String = "hikari_open_connections"

   override suspend fun check(): HealthCheckResult {
      val conns = ds.hikariPoolMXBean.totalConnections
      val msg = "$conns connection(s) to Hikari db-pool ${ds.poolName} [minConnections:$minConnections]"
      return if (conns >= minConnections) {
         HealthCheckResult.healthy(msg)
      } else {
         HealthCheckResult.unhealthy(msg, null)
      }
   }
}

fun createHikariDS(): HikariDataSource =
   HikariConfig().apply {
      jdbcUrl = "jdbc:h2:mem:kjs;DB_CLOSE_DELAY=-1"
      username = "sa"
      password = ""
      maximumPoolSize = 1
   }.let { HikariDataSource(it) }

fun createHealthChecks(ds: HikariDataSource): HealthCheckRegistry =
   HealthCheckRegistry(Dispatchers.Default) {
      register(HikariConnectionsHealthCheck(ds, 1), 1.seconds)
   }

fun main() {
   val dataSource = createHikariDS()
   println("Created datasource $dataSource")
   val registry = createHealthChecks(dataSource)
   Thread.sleep(5000) // let the healthcheck run a few times
   dataSource.close()
   println("Closed datasource ${dataSource.isClosed}")
   registry.close()
}
