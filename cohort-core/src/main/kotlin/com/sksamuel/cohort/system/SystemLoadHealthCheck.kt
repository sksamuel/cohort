package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] for the maximum system load.
 *
 * The check is considered healthy if the system load is < [maxLoad].
 */
class SystemLoadHealthCheck(private val maxLoad: Double) : HealthCheck {

  private val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

  override val name: String = "system_cpu_average"

  override suspend fun check(): HealthCheckResult {
    val load = bean.systemLoadAverage
    return if (load < maxLoad) {
      HealthCheckResult.Healthy("System load is below threshold [$load < $maxLoad]")
    } else {
      HealthCheckResult.Unhealthy("System load is above threshold [$load >= $maxLoad]", null)
    }
  }

}
