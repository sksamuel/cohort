@file:Suppress("unused")

package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * A [HealthCheckRegistry] executes [HealthCheck]s based on provided schedules.
 *
 * All executions happen on the provided [CoroutineDispatcher].
 *
 * Individual checks are free to shift the dispatcher onto a [Dispatchers.IO] for IO calls.
 * It is recommended that the provided dispatcher has parallelism limited to 1.
 *
 * This registry creates one additional thread for its own use, to run the scheduler. This thread remains
 * in [Thread.State.BLOCKED] while waiting to fire a scheduled event, and this thread is not used
 * for the actual execution of the scheduled events.
 */
class HealthCheckRegistry(private val dispatcher: CoroutineDispatcher) {

  private val scheduler = Executors.newScheduledThreadPool(1)
  private val names = mutableSetOf<String>()
  private val results = ConcurrentHashMap<String, CheckStatus>()

  /**
   * Adds a new [HealthCheck] to this registry using the given duration for both initial delay and intervals.
   * The name is derived from the check class.
   */
  fun register(
    check: HealthCheck,
    delay: Duration,
  ): HealthCheckRegistry = register(check::class.java.name, check, delay, delay)

  /**
   * Adds a new [HealthCheck] to this registry with the given schedule.
   *
   * @param name the name is associated with the result in the output json.
   * This is useful to allow the same check to be registered multiple times against different configurations.
   */
  fun register(
    name: String,
    check: HealthCheck,
    initialDelay: Duration,
    checkInterval: Duration
  ): HealthCheckRegistry {
    if (names.contains(name)) error("Check $name already registered")
    names.add(name)
    schedule(name, check, initialDelay, checkInterval)
    return this
  }

  private fun schedule(name: String, check: HealthCheck, thisDelay: Duration, nextDelay: Duration) {
    scheduler.schedule(
      {
        GlobalScope.launch(dispatcher) {
          run(name, check, nextDelay)
        }
      },
      thisDelay.inWholeMilliseconds,
      TimeUnit.MILLISECONDS
    )
  }

  private suspend fun run(name: String, check: HealthCheck, delay: Duration) {
    try {
      when (val result = check.check()) {
        is HealthCheckResult.Healthy -> success(name, result)
        is HealthCheckResult.Unhealthy -> failure(name, result)
      }
    } catch (t: Throwable) {
      val result = HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
      failure(name, result)
    }
    schedule(name, check, delay, delay)
  }

  private fun success(name: String, result: HealthCheckResult.Healthy) {

    val previous = results[name]
    val successes = if (previous == null) 1 else previous.consecutiveSuccesses + 1

    results[name] = CheckStatus(
      consecutiveSuccesses = successes,
      consecutiveFailures = 0, // reset to 0 when we have a success
      healthy = true,
      timestamp = Instant.now(),
      result = result
    )
  }

  private fun failure(name: String, result: HealthCheckResult.Unhealthy) {

    val previous = results[name]
    val failures = if (previous == null) 1 else previous.consecutiveFailures + 1

    results[name] = CheckStatus(
      consecutiveSuccesses = 0, // reset to 0 when we have a failure
      consecutiveFailures = failures,
      healthy = false,
      timestamp = Instant.now(),
      result = result
    )
  }

  fun status(): Health {
    val unhealthy = results.values.any { !it.healthy }
    return Health(!unhealthy, results.toMap())
  }
}


data class CheckStatus(
  val consecutiveSuccesses: Int,
  val consecutiveFailures: Int,
  val healthy: Boolean, // overall health status
  val timestamp: Instant,
  val result: HealthCheckResult,
)

data class Health(val healthy: Boolean, val results: Map<String, CheckStatus>)

fun <A> Health.fold(
  ifUnhealthy: (Map<String, CheckStatus>) -> A,
  ifHealthy: (Map<String, CheckStatus>) -> A
): A {
  return when (healthy) {
    true -> ifHealthy(results)
    false -> ifUnhealthy(results)
  }
}
