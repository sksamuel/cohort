package com.sksamuel.healthcheck

import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean

class ThreadDeadlockHealthCheck(private val bean: ThreadMXBean = ManagementFactory.getThreadMXBean()) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val threads = bean.findDeadlockedThreads() ?: longArrayOf()
    val msg = "There are $threads deadlocked threads"
    return if (threads.isEmpty()) HealthCheckResult.Healthy(msg) else HealthCheckResult.Unhealthy(msg, null)
  }
}
