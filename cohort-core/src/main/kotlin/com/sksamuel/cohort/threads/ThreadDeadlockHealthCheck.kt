package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * A Cohort [HealthCheck] that checks for the presence of deadlocked threads.
 *
 * The check is considered healthy if the deadlock count is < [maxDeadlocks].
 */
class ThreadDeadlockHealthCheck(
  private val maxDeadlocks: Int = 1,
  private val bean: ThreadMXBean = ManagementFactory.getThreadMXBean()
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val deadlocked = bean.findDeadlockedThreads()?.size ?: 0
    val msg = "There are $deadlocked deadlocked threads"
    return if (deadlocked < maxDeadlocks) HealthCheckResult.Healthy(msg) else HealthCheckResult.Unhealthy(msg, null)
  }
}
