package com.sksamuel.cohort

import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

class ThreadDeadlockCheck(private val bean: ThreadMXBean = ManagementFactory.getThreadMXBean()) : Check {

  override fun check(): CheckResult {
    val deadlocked = bean.findDeadlockedThreads()?.size ?: 0
    val msg = "There are $deadlocked deadlocked threads"
    return if (deadlocked == 0) CheckResult.Healthy(msg) else CheckResult.Unhealthy(msg, null)
  }
}
