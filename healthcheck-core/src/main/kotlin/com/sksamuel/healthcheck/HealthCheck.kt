package com.sksamuel.healthcheck

interface HealthCheck {
  suspend fun check(): HealthCheckResult
}

sealed class HealthCheckResult {
  data class Healthy(val message: String?) : HealthCheckResult()
  data class Unhealthy(val message: String, val cause: Throwable?) : HealthCheckResult()
}
