package com.sksamuel.healthcheck

class HealthCheckRegistry(private val healthchecks: List<HealthCheck>) {

  fun register(healthCheck: HealthCheck): HealthCheckRegistry {
    return HealthCheckRegistry(healthchecks + healthCheck)
  }

  fun execute(): List<HealthCheckResult> {
    return healthchecks.map { it.check() }
  }
}
