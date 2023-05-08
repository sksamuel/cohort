package com.sksamuel.cohort.cpu

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] that is healthy once the total HotSpot compilation time has
 * reached [time].
 *
 * The level will vary depending on your service.
 * 2000 is a fair value for most smaller microservices.
 */
class HotSpotCompilationTimeHealthCheck(private val time: Long = 2000) : HealthCheck {
   override suspend fun check(): HealthCheckResult {
      val compilationTime = ManagementFactory.getCompilationMXBean().totalCompilationTime
      return if (compilationTime >= time)
         HealthCheckResult.healthy("Compilation time: $compilationTime (required $time)")
      else
         HealthCheckResult.unhealthy("Compilation time: $compilationTime (required $time)")
   }
}
