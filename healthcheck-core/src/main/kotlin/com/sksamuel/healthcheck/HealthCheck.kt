package com.sksamuel.healthcheck

interface HealthCheck {
  suspend fun check(): HealthCheckResult
}

sealed class HealthCheckResult {

  val isHealthy: Boolean by lazy { this is Healthy }

  data class Healthy(val message: String?) : HealthCheckResult()
  data class Unhealthy(val message: String, val cause: Throwable?) : HealthCheckResult()
}
