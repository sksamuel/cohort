package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] that the number of live threads does not exceed a value.
 *
 * The check is considered healthy if the current thread count <= [maxThreadCount].
 */
class LiveThreadsHealthCheck(private val maxThreadCount: Int) : HealthCheck {

  private val threadBean = ManagementFactory.getThreadMXBean()

  override val name: String = "live_threads"

  override suspend fun check(): HealthCheckResult {
    val count = threadBean.threadCount
    return if (count <= maxThreadCount) {
      HealthCheckResult.Healthy("Live threads is below threshold [$count <= $maxThreadCount]")
    } else {
      HealthCheckResult.Unhealthy("Live threads is above threshold [$count > $maxThreadCount]", null)
    }
  }
}
