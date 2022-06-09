package com.sksamuel.cohort.dbcp

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.commons.dbcp2.BasicDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of connections in Apache DBCP2 [BasicDataSource].
 *
 * This is useful to detect when connections are unable to be made.
 *
 * The check is considered healthy if the total connection count (sum of idle and active) is >= [minConnections].
 */
class DbcpConnectionsHealthCheck(
  private val ds: BasicDataSource,
  private val minConnections: Int,
) : HealthCheck {
  override suspend fun check(): HealthCheckResult {
    val total = ds.numActive + ds.numIdle
    val msg = "Database connections is $total [min required is $minConnections]"
    return if (total >= minConnections) HealthCheckResult.Healthy(msg) else HealthCheckResult.Unhealthy(msg, null)
  }
}
