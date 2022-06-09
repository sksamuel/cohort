package com.sksamuel.cohort.dbcp

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.commons.dbcp2.BasicDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of idle connections in a Apache DBCP2 [BasicDataSource].
 *
 * This is useful to detect when connections are being exhausted.
 *
 * The check is considered healthy if the idle count is >= [minIdle].
 */
class DbcpMinIdleHealthCheck(
  private val ds: BasicDataSource,
  private val minIdle: Int,
) : HealthCheck {
  override suspend fun check(): HealthCheckResult {
    val msg = "Idle connections ${ds.numIdle} [min required is $minIdle]"
    return if (ds.numIdle >= minIdle) HealthCheckResult.Healthy(msg) else HealthCheckResult.Unhealthy(msg, null)
  }
}
