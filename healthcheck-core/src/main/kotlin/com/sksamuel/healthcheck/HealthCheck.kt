package com.sksamuel.healthcheck

interface HealthCheck {
  fun check(): HealthCheckResult
}

sealed class HealthCheckResult {
  object Healthy : HealthCheckResult()
  data class Unhealthy(val message: String, val cause: Throwable?) : HealthCheckResult()
}
