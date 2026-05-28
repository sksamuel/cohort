package com.sksamuel.cohort.threads

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that the number of threads in a given state does not exceed a value.
 *
 * The check is considered healthy if the count of threads in that state is <= [maxCount].
 */
class ThreadStateHealthCheck(
  private val state: Thread.State,
  private val maxCount: Int
) : HealthCheck {

  // Encode the monitored state in the name so two checks (e.g. BLOCKED and WAITING) can be
  // registered using the default register(check) overload. With a single "thread_state" name,
  // HealthCheckRegistry.register threw "Check thread_state already registered" on the second
  // registration, making the canonical use-case impossible without manually naming each check.
  override val name: String = "thread_state_${state.name.lowercase()}"

  override suspend fun check(): HealthCheckResult {
    val count = Thread.getAllStackTraces().keys.count { it.state == state }
    return if (count <= maxCount) {
      HealthCheckResult.healthy("Thread count for state $state is below threshold [$count <= $maxCount]")
    } else {
      HealthCheckResult.unhealthy("Thread count for state $state is above threshold [$count > $maxCount]", null)
    }
  }
}
