package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * A Cohort [HealthCheck] that the number of peak threads does not exceed a value.
 *
 * The check is considered healthy if the peak thread count <= [maxPeakThreads].
 */
class PeakThreadsHealthCheck(
  private val maxPeakThreads: Int,
  private val threadBean: ThreadMXBean = ManagementFactory.getThreadMXBean(),
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val count = threadBean.peakThreadCount
    return if (count <= maxPeakThreads) {
      HealthCheckResult.Healthy("Peak threads is below threshold [$count <= $maxPeakThreads]")
    } else {
      HealthCheckResult.Unhealthy("Peak threads is above threshold [$count > $maxPeakThreads]", null)
    }
  }
}
