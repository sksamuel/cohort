package com.sksamuel.cohort

/**
 * A [HealthCheck] is invoked periodically, and returns a [HealthCheckResult]
 * which indicates the health of the system.
 */
fun interface HealthCheck {

   suspend fun check(): HealthCheckResult

   // the default name used in logs and metrics unless overridden when registering or subclassing.
   val name: String
      get() {
         val jvmName = this::class.java.name
         return if (jvmName.startsWith("com.sksamuel.cohort"))
            jvmName.removePrefix("com.sksamuel.")
         else jvmName
      }

   companion object {

      /**
       * Creates a [HealthCheck] by using the [Result] of a function to indicate health.
       */
      operator fun invoke(f: () -> Result<String>) = HealthCheck {
         f().fold(
            { HealthCheckResult.healthy(it) },
            { HealthCheckResult.unhealthy(it.message ?: it::class.java.name, it) })
      }
   }
}

/**
 * The result of a [HealthCheck].
 */
data class HealthCheckResult(
   val status: HealthStatus,
   val message: String,
   val cause: Throwable?,
) {

   val isHealthy = status == HealthStatus.Healthy

   companion object {
      fun healthy(message: String) = HealthCheckResult(HealthStatus.Healthy, message, null)
      fun unhealthy(message: String, cause: Throwable? = null) =
         HealthCheckResult(HealthStatus.Unhealthy, message, cause)
   }
}

/**
 * The result of a [HealthCheck].
 */
enum class HealthStatus {
   Healthy, Unhealthy
}
