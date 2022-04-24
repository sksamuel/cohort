package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] that the number of daemon threads does not exceed a value.
 *
 * The check is considered healthy if the daemon thread count <= [maxDaemonThreads].
 */
class DaemonThreadsHealthCheck(private val maxDaemonThreads: Int) : HealthCheck {

  private val threadBean = ManagementFactory.getThreadMXBean()

  override suspend fun check(): HealthCheckResult {
    val count = threadBean.daemonThreadCount
    val msg = "Daemon threads count is $count [threshold is $maxDaemonThreads]"
    return if (count <= maxDaemonThreads) {
      HealthCheckResult.Healthy(msg)
    } else {
      HealthCheckResult.Unhealthy(msg, null)
    }
  }
}
