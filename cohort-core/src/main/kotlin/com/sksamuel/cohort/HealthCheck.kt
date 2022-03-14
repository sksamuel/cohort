package com.sksamuel.cohort

interface HealthCheck {
  suspend fun check(): CheckResult
}

/**
 * The result of a [HealthCheck].
 *
 * Can be either [Healthy] or [Unhealthy].
 */
sealed class CheckResult {

  val isHealthy: Boolean by lazy { this is Healthy }

  abstract val message: String?
  abstract val cause: Throwable?

  data class Healthy(override val message: String?) : CheckResult() {
    override val cause: Throwable? = null
  }

  data class Unhealthy(override val message: String, override val cause: Throwable?) : CheckResult()
}
