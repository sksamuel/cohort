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

  override val name: String = "peak_threads"

  override suspend fun check(): HealthCheckResult {
    val count = threadBean.peakThreadCount
    val msg = "Peak threads is $count [threshold is $maxPeakThreads]"
    return if (count <= maxPeakThreads) {
      HealthCheckResult.Healthy(msg)
    } else {
      HealthCheckResult.Unhealthy(msg, null)
    }
  }
}
