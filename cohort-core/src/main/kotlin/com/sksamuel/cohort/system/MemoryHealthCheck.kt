package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks free memory in the system.
 *
 * The check is considered healthy if the amount of free memory is above [minFreeMb].
 */
class MemoryHealthCheck(private val minFreeMb: Int) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val free = Runtime.getRuntime().freeMemory()
    val freeMb = free / 1000_000
    return if (freeMb < minFreeMb) {
      HealthCheckResult.Unhealthy("Freemem is below threshold [$freeMb < $minFreeMb]", null)
    } else {
      HealthCheckResult.Healthy("Freemem is above threshold [$freeMb >= $minFreeMb]")
    }
  }

}
