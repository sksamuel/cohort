package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] for the maximum system cpu between 0 and 1.0
 *
 * The check is considered healthy if the system cpu load is < [maxLoad].
 */
class SystemCpuHealthCheck(private val maxLoad: Double) : HealthCheck {

  private val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

  override suspend fun check(): HealthCheckResult {
    val load = bean.systemCpuLoad
    return if (load < maxLoad) {
      HealthCheckResult.Healthy("System CPU is below threshold [$load < $maxLoad]")
    } else {
      HealthCheckResult.Unhealthy("System CPU is above threshold [$load >= $maxLoad]", null)
    }
  }

}
