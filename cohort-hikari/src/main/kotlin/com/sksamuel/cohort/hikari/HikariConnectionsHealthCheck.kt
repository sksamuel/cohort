package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of connections in a [HikariDataSource].
 *
 * This is useful to ensure a service has opened a required number of connections before being
 * considered healthy.
 *
 * The check is considered healthy if the total connection count - idle and active - is >= [minConnections].
 */
class HikariConnectionsHealthCheck(
   private val ds: HikariDataSource,
   private val minConnections: Int,
   override val name: String = "hikari_open_connections",
) : HealthCheck {

   init {
      // Without this guard, minConnections=0 makes the check vacuously healthy (totalConnections
      // is always >= 0), so a pool that failed to initialize would still report green.
      require(minConnections > 0) { "minConnections must be > 0, was $minConnections" }
   }

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
