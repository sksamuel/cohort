package com.sksamuel.cohort.threads

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import java.lang.management.ManagementFactory

/**
 * A Cohort [Check] that the number of live threads does not exceed a value.
 *
 * The check is considered healthy if the current thread count <= [maxThreadCount].
 */
class LiveThreadsCheck(private val maxThreadCount: Int) : Check {

  private val threadBean = ManagementFactory.getThreadMXBean()

  override suspend fun check(): CheckResult {
    val count = threadBean.threadCount
    return if (count <= maxThreadCount) {
      CheckResult.Healthy("Thread count is below threshold [$count < $maxThreadCount]")
    } else {
      CheckResult.Healthy("System CPU is below threshold [$count < $maxThreadCount]")
    }
  }
}
