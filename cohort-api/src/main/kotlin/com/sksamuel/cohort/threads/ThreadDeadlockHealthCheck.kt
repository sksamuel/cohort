package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

/**
 * A Cohort [HealthCheck] that checks for the presence of deadlocked threads.
 *
 * The check is considered healthy if the deadlock count is <= [maxDeadlocks].
 */
class ThreadDeadlockHealthCheck(
  // A deadlock involves >= 2 threads, so the only sane default is 0 — any deadlocked thread
  // means the JVM has a problem. The previous default of 1 paired with a strict `<` made
  // "one deadlocked thread" report healthy, which is never what anyone wants.
  private val maxDeadlocks: Int = 0,
  private val bean: ThreadMXBean = ManagementFactory.getThreadMXBean()
) : HealthCheck {

  override val name: String = "thread_deadlocks"

  override suspend fun check(): HealthCheckResult {
    val deadlocked = bean.findDeadlockedThreads()?.size ?: 0
    val msg = "There are $deadlocked deadlocked threads"
    return if (deadlocked <= maxDeadlocks) HealthCheckResult.healthy(msg) else HealthCheckResult.unhealthy(msg, null)
  }
}
