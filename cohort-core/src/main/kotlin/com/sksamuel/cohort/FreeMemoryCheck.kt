package com.sksamuel.cohort

class FreeMemoryCheck(private val minFreeMb: Int) : Check {
  override fun check(): CheckResult {
    val free = Runtime.getRuntime().freeMemory()
    val freeMb = free / 1000_000
    return if (freeMb < minFreeMb) {
      CheckResult.Unhealthy("Freemem is below threshold [$freeMb < $minFreeMb]", null)
    } else {
      CheckResult.Healthy("Freemem is above threshold [$freeMb >= $minFreeMb]")
    }
  }
}
