package com.sksamuel.cohort

import com.sksamuel.cohort.HealthCheckResult.Healthy
import com.sksamuel.cohort.HealthCheckResult.Unhealthy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

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
 * Interface for [HealthCheck]s that are designed to warm up the JVM.
 */
abstract class WarmupHealthCheck : HealthCheck {

   abstract val iterations: Int
   abstract val interval: Duration

   abstract suspend fun warmup()

   private val completed = AtomicInteger(0)

   fun start(scope: CoroutineScope) {
      scope.launch {
         repeat(iterations) {
            warmup()
            completed.incrementAndGet()
            delay(interval)
         }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return if (completed.get() == iterations)
         Healthy("Warmup iterations completed: ${completed.get()}")
      else
         Unhealthy("Warmup iterations completed: ${completed.get()}")
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
