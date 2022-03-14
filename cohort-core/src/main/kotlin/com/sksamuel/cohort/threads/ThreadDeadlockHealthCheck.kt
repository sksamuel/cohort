package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.CheckResult
import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

class ThreadDeadlockHealthCheck(
  private val bean: ThreadMXBean = ManagementFactory.getThreadMXBean()
) : HealthCheck {

  override suspend fun check(): CheckResult {
    val deadlocked = bean.findDeadlockedThreads()?.size ?: 0
    val msg = "There are $deadlocked deadlocked threads"
    return if (deadlocked == 0) CheckResult.Healthy(msg) else CheckResult.Unhealthy(msg, null)
  }
}
