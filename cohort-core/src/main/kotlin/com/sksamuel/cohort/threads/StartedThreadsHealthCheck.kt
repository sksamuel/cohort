package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * A Cohort [HealthCheck] that the number of created and started threads does not exceed a value.
 *
 * This can help find issues where a framework is consistently creating new threads leading to resource starvation.
 *
 * The check is considered healthy if the started thread count <= [maxStartedThreads].
 */
class StartedThreadsHealthCheck(
  private val maxStartedThreads: Int,
  private val threadMXBean: ThreadMXBean = ManagementFactory.getThreadMXBean(),
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val count = threadMXBean.totalStartedThreadCount
    return if (count <= maxStartedThreads) {
      HealthCheckResult.Healthy("Started threads is below threshold [$count <= $maxStartedThreads]")
    } else {
      HealthCheckResult.Unhealthy("Started threads is above threshold [$count > $maxStartedThreads]", null)
    }
  }
}
