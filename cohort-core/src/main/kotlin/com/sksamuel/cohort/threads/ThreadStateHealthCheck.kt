package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that the number of threads in a given state does not exceed a value.
 *
 * The check is considered healthy if the count of threads in that state is <= [maxCount].
 */
class ThreadStateHealthCheck(
  private val state: Thread.State,
  private val maxCount: Int
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val count = Thread.getAllStackTraces().keys.count { it.state == state }
    return if (count <= maxCount) {
      HealthCheckResult.Healthy("Thread count for state $state is below threshold [$count < $maxCount]")
    } else {
      HealthCheckResult.Unhealthy("Thread count for state $state is above threshold [$count < $maxCount]", null)
    }
  }
}
