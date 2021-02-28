package com.sksamuel.healthcheck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class HealthCheckRegistry(private val dispatcher: CoroutineDispatcher) {

  private val scheduler = Executors.newScheduledThreadPool(1)
  private val results = ConcurrentHashMap<String, HealthCheckResult>()

  fun register(
    name: String,
    healthcheck: HealthCheck,
    interval: Duration,
    runImmediately: Boolean = true
  ): HealthCheckRegistry {

    val millis = interval.toLongMilliseconds()
    scheduler.scheduleAtFixedRate({
      GlobalScope.launch(dispatcher) {
        val result = try {
          healthcheck.check()
        } catch (t: Throwable) {
          HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
        }
        results[name] = result
      }
    }, if (runImmediately) 0 else millis, millis, TimeUnit.MILLISECONDS)
    return this
  }

  fun status(): HealthStatus {
    val unhealthy = results.values.any { it is HealthCheckResult.Unhealthy }
    return HealthStatus(!unhealthy, results.toMap())
  }
}

data class HealthStatus(val healthy: Boolean, val results: Map<String, HealthCheckResult>)

fun <A> HealthStatus.fold(
  ifUnhealthy: (Map<String, HealthCheckResult>) -> A,
  ifHealthy: (Map<String, HealthCheckResult>) -> A
): A {
  return when (healthy) {
    true -> ifHealthy(results)
    false -> ifUnhealthy(results)
  }
}
