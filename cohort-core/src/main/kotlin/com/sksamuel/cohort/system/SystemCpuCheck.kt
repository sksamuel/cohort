package com.sksamuel.cohort.system

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [Check] for the maximum system cpu between 0 and 1.0
 *
 * The check is considered healthy if the system cpu load is < [maxLoad].
 */
class SystemCpuCheck(private val maxLoad: Double) : Check {

  override suspend fun check(): CheckResult {

    val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    val load = bean.systemCpuLoad
    return if (load < maxLoad) {
      CheckResult.Unhealthy("System CPU is below threshold [$load < $maxLoad]", null)
    } else {
      CheckResult.Healthy("System CPU is above threshold [$load >= $maxLoad]")
    }
  }

}
