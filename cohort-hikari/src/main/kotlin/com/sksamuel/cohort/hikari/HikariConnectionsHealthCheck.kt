package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of connections in a [HikariDataSource].
 *
 * This is useful to detect when connections are unable to be made.
 *
 * The check is considered healthy if the total connection count (sum of idle and active) is >= [minConnections].
 */
class HikariConnectionsHealthCheck(
  private val ds: HikariDataSource,
  private val minConnections: Int,
) : HealthCheck {

  override val name: String = "hikari_open_connections"

  override suspend fun check(): HealthCheckResult {
    val conns = ds.hikariPoolMXBean.totalConnections
    val msg = "$conns connection(s) to Hikari db-pool ${ds.poolName} [$minConnections minConnections]"
    return if (conns >= minConnections) {
      HealthCheckResult.Healthy(msg)
    } else {
      HealthCheckResult.Unhealthy(msg, null)
    }
  }
}
