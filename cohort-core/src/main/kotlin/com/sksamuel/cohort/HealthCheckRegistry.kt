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
 * Defines the schedule for running a [HealthCheck].
 *
 * @param checkInterval how often to initiate this check.
 * @param initialDelay the delay before the first check is executed.
 */
data class Schedule(
  val checkInterval: Duration,
  val downtimeInterval: Duration,
  val initialDelay: Duration,
  val failureAttempts: Int,
  val successAttempts: Int,
) {
  init {
    require(successAttempts > 0)
    require(failureAttempts > 0)
  }

  constructor(interval: Duration) : this(interval, interval, Duration.ZERO, 1, 1)
  constructor(interval: Duration, failureAttempts: Int) : this(interval, interval, Duration.ZERO, failureAttempts, 1)

}

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
   * Adds a new [HealthCheck] to this registry with the given [schedule].
   */
  fun register(
    check: HealthCheck,
    schedule: Schedule
  ): HealthCheckRegistry = register(check::class.java.name, check, schedule)

  /**
   * Adds a new [HealthCheck] to this registry with the given [schedule].
   *
   * @param name the name is associated with the result in the output json.
   * This is useful to allow the same check to be registered multiple times against different configurations.
   */
  fun register(
    name: String,
    check: HealthCheck,
    schedule: Schedule,
  ): HealthCheckRegistry {
    if (names.contains(name)) error("Check $name already registered")
    names.add(name)
    schedule(name, check, schedule, schedule.initialDelay)
    return this
  }

  private fun schedule(name: String, check: HealthCheck, schedule: Schedule, duration: Duration) {
    scheduler.schedule(
      {
        GlobalScope.launch(dispatcher) {
          run(name, check, schedule)
        }
      },
      duration.inWholeMilliseconds,
      TimeUnit.MILLISECONDS
    )
  }

  private suspend fun run(name: String, check: HealthCheck, schedule: Schedule) {
    try {
      when (val result = check.check()) {
        is HealthCheckResult.Healthy -> success(name, result)
        is HealthCheckResult.Unhealthy -> failure(name, result)
      }
    } catch (t: Throwable) {
      val result = HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
      failure(name, result)
    }
    schedule(name, check, schedule, schedule.checkInterval)
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
