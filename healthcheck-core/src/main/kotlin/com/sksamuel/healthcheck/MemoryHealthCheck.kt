package com.sksamuel.healthcheck

class MemoryHealthCheck(private val minFreeMb: Int) : HealthCheck {
  override fun check(): HealthCheckResult {
    val free = Runtime.getRuntime().freeMemory()
    val freeMb = free * 1000_000
    return if (freeMb < minFreeMb) {
      HealthCheckResult.Unhealthy("Freemem is below threshold [$freeMb < $minFreeMb]", null)
    } else {
      HealthCheckResult.Healthy("Freemem is above threshold [$freeMb >= $minFreeMb]")
    }
  }
}
