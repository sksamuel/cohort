package com.sksamuel.cohort.cpu

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] that the maximum system cpu is below a threshold.
 * Values are in the range 0 and 1.0.
 *
 * The check is considered healthy if the system cpu load is < [maxLoad].
 */
class ProcessCpuHealthCheck(private val maxLoad: Double) : HealthCheck {

   private val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean

   override val name: String = "process_cpu_load"

   override suspend fun check(): HealthCheckResult {
      val load = bean.processCpuLoad
      val msg = "Process CPU $load [max load $maxLoad]"
      return if (load < maxLoad) {
         HealthCheckResult.healthy(msg)
      } else {
         HealthCheckResult.unhealthy(msg, null)
      }
   }
}
