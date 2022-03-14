package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.zaxxer.hikari.HikariDataSource

/**
 * A Cohort [HealthCheck] that checks for the number of threads awaiting a connection
 * in a [HikariDataSource].
 *
 * This is useful to detect when queries are running slowly and causing threads to back up
 * waiting for a connection.
 *
 * The check is considered healthy if the current count of awaiting threads is <= [maxAwaiting].
 */
class ThreadsAwaitingConnectionHealthCheck(
  private val ds: HikariDataSource,
  private val maxAwaiting: Int,
) : HealthCheck {
  override suspend fun check(): HealthCheckResult {
    val awaiting = ds.hikariPoolMXBean.threadsAwaitingConnection
    return if (awaiting <= maxAwaiting) {
      HealthCheckResult.Healthy("Threads awaiting database connection is equal or above threshold [$awaiting <= $maxAwaiting]")
    } else {
      HealthCheckResult.Unhealthy(
        "Threads awaiting database connection is below threshold [$awaiting > $maxAwaiting]",
        null
      )
    }
  }
}
