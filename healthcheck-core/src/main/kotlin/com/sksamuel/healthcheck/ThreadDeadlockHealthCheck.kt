package com.sksamuel.healthcheck

import java.lang.management.ManagementFactory

class ThreadDeadlockHealthCheck : HealthCheck {

  private val threads = ManagementFactory.getThreadMXBean()

  override fun check(): HealthCheckResult {
    val threads = threads.findDeadlockedThreads()
    val msg = "There are $threads deadlocked threads"
    return if (threads.isEmpty()) HealthCheckResult.Healthy(msg) else HealthCheckResult.Unhealthy(msg, null)
  }
}
