package com.sksamuel.cohort

import com.sksamuel.cohort.HealthCheckResult.Healthy
import com.sksamuel.cohort.HealthCheckResult.Unhealthy

fun interface HealthCheck {

   suspend fun check(): HealthCheckResult

   val name: String
      get() = this::class.java.name.removePrefix("com.sksamuel.")

   companion object {
      operator fun invoke(f: () -> Result<String>) = HealthCheck {
         f().fold(
            { Healthy(it) },
            { Unhealthy(it.message ?: it::class.java.name, it) })
      }
   }
}

/**
 * The result of a [HealthCheck].
 *
 * Can be either [Healthy] or [Unhealthy].
 */
sealed class HealthCheckResult {

   abstract val isHealthy: Boolean
   abstract val message: String?
   abstract val cause: Throwable?

   data class Healthy(override val message: String?) : HealthCheckResult() {
      override val cause: Throwable? = null
      override val isHealthy: Boolean = true
   }

   data class Unhealthy(override val message: String, override val cause: Throwable?) : HealthCheckResult() {
      constructor(message: String) : this(message, null)
      override val isHealthy: Boolean = false
   }
}
