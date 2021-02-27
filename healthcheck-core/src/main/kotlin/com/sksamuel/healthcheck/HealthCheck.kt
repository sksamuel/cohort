package com.sksamuel.healthcheck

interface HealthCheck {
  fun check(): HealthCheckResult
}

sealed class HealthCheckResult {
  data class Healthy(val message: String?) : HealthCheckResult()
  data class Unhealthy(val message: String, val cause: Throwable?) : HealthCheckResult()
}
