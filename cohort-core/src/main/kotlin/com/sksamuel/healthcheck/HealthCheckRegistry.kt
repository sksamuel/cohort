@file:Suppress("unused")

package com.sksamuel.healthcheck

import java.sql.Timestamp
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

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

class HealthCheckRegistry(threads: Int) {

  private val scheduler = Executors.newScheduledThreadPool(threads)
  private val results = ConcurrentHashMap<String, HealthCheckStatus>()

  fun register(
    name: String,
    healthcheck: HealthCheck,
    schedule: Schedule
  ): HealthCheckRegistry {

    scheduler.schedule(
      { run(name, healthcheck, schedule) },
      schedule.initialDelay.toLongMilliseconds(),
      TimeUnit.MILLISECONDS
    )

    return this
  }

  private fun run(name: String, healthcheck: HealthCheck, schedule: Schedule) {
    try {
      when (val result = healthcheck.check()) {
        is HealthCheckResult.Healthy -> success(name, result, schedule, healthcheck)
        is HealthCheckResult.Unhealthy -> failure(name, result, schedule, healthcheck)
      }
    } catch (t: Throwable) {
      val result = HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
      failure(name, result, schedule, healthcheck)
    }
  }

  private fun success(name: String, result: HealthCheckResult.Healthy, schedule: Schedule, healthcheck: HealthCheck) {

    val previous = results[name]
    val timestamp = Timestamp.from(Instant.now())
    val successes = if (previous == null) 1 else previous.consecutiveSuccesses + 1

    results[name] = HealthCheckStatus(
      consecutiveSuccesses = successes,
      consecutiveFailures = 0, // reset to 0 when we have a success
      healthy = if (successes >= schedule.successAttempts) true else previous?.healthy ?: true,
      timestamp = timestamp,
      result = result
    )

    scheduler.schedule(
      { run(name, healthcheck, schedule) },
      schedule.checkInterval.toLongMilliseconds(),
      TimeUnit.MILLISECONDS
    )
  }

  private fun failure(name: String, result: HealthCheckResult.Unhealthy, schedule: Schedule, healthcheck: HealthCheck) {

    val previous = results[name]
    val timestamp = Timestamp.from(Instant.now())
    val failures = if (previous == null) 1 else previous.consecutiveFailures + 1

    results[name] = HealthCheckStatus(
      consecutiveSuccesses = 0, // reset to 0 when we have a failure
      consecutiveFailures = failures,
      healthy = if (failures >= schedule.failureAttempts) false else previous?.healthy ?: false,
      timestamp = timestamp,
      result = result
    )

    scheduler.schedule(
      { run(name, healthcheck, schedule) },
      schedule.downtimeInterval.toLongMilliseconds(),
      TimeUnit.MILLISECONDS
    )
  }

  fun status(): HealthStatus {
    val unhealthy = results.values.any { !it.healthy }
    return HealthStatus(!unhealthy, results.toMap())
  }
}


data class HealthCheckStatus(
  val consecutiveSuccesses: Int,
  val consecutiveFailures: Int,
  val healthy: Boolean,
  val timestamp: Timestamp,
  val result: HealthCheckResult
)

data class HealthStatus(val healthy: Boolean, val results: Map<String, HealthCheckStatus>)

fun <A> HealthStatus.fold(
  ifUnhealthy: (Map<String, HealthCheckStatus>) -> A,
  ifHealthy: (Map<String, HealthCheckStatus>) -> A
): A {
  return when (healthy) {
    true -> ifHealthy(results)
    false -> ifUnhealthy(results)
  }
}
