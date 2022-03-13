package com.sksamuel.cohort.system

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult

/**
 * A Cohort [Check] that checks free memory in the system.
 * The check is considered healthy if the amount of free memory is above [minFreeMb].
 */
class MemoryCheck(private val minFreeMb: Int) : Check {

  override suspend fun check(): CheckResult {
    val free = Runtime.getRuntime().freeMemory()
    val freeMb = free / 1000_000
    return if (freeMb < minFreeMb) {
      CheckResult.Unhealthy("Freemem is below threshold [$freeMb < $minFreeMb]", null)
    } else {
      CheckResult.Healthy("Freemem is above threshold [$freeMb >= $minFreeMb]")
    }
  }

}
