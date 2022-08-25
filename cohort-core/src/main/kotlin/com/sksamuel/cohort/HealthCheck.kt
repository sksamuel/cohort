package com.sksamuel.cohort

fun interface HealthCheck {
  suspend fun check(): HealthCheckResult

  companion object {
    operator fun invoke(f: () -> Result<String>) = HealthCheck {
      f().fold(
        { HealthCheckResult.Healthy(it) },
        { HealthCheckResult.Unhealthy(it.message ?: it::class.java.name, it) })
    }
  }
}

/**
 * The result of a [HealthCheck].
 *
 * Can be either [Healthy] or [Unhealthy].
 */
sealed class HealthCheckResult {

  val isHealthy: Boolean by lazy { this is Healthy }

  abstract val message: String?
  abstract val cause: Throwable?

  data class Healthy(override val message: String?) : HealthCheckResult() {
    override val cause: Throwable? = null
  }

  data class Unhealthy(override val message: String, override val cause: Throwable?) : HealthCheckResult() {
    constructor(message: String) : this(message, null)
  }
}
