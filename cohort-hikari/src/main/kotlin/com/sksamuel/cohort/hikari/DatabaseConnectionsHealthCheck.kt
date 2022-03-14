package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of connections in a Hikari datasource.
 *
 * This is useful to detect when connections are unable to be made.
 *
 * The check is considered healthy if the current connection count (sum of idle and active) is >= [minConnections].
 */
class DatabaseConnectionsHealthCheck(
  private val ds: HikariDataSource,
  private val minConnections: Int,
) : HealthCheck {
  override suspend fun check(): HealthCheckResult {
    val conns = ds.hikariPoolMXBean.totalConnections
    return if (conns >= minConnections) {
      HealthCheckResult.Healthy("Database connections is equal or above threshold [$conns >= $minConnections]")
    } else {
      HealthCheckResult.Unhealthy("Database connections is below threshold [$conns < $minConnections]", null)
    }
  }
}
