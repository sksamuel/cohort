package com.sksamuel.healthcheck

interface HealthCheck {
  suspend fun check(): HealthCheckResult
}

sealed class HealthCheckResult {

  val isHealthy: Boolean by lazy { this is Healthy }
  abstract val message: String?
  abstract val cause: Throwable?

  data class Healthy(override val message: String?) : HealthCheckResult() {
    override val cause: Throwable? = null
  }

  data class Unhealthy(override val message: String, override val cause: Throwable?) : HealthCheckResult()
}
