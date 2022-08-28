package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of idle connections in a [HikariDataSource].
 *
 * This is useful to detect when connections are being exhausted.
 *
 * The check is considered healthy if the number of idle connections >= [minIdle].
 */
class HikariMinIdleHealthCheck(
  private val ds: HikariDataSource,
  private val minIdle: Int,
) : HealthCheck {

  override val name: String = "hikari_min_idle"

  override suspend fun check(): HealthCheckResult {
    val idleConnections = ds.hikariPoolMXBean.idleConnections
    val msg = "Idle connections $idleConnections [min required is $minIdle]"
    return if (idleConnections >= minIdle) {
      HealthCheckResult.Healthy(msg)
    } else {
      HealthCheckResult.Unhealthy(msg, null)
    }
  }
}
