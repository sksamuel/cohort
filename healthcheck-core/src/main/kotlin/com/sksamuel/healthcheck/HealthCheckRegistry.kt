package com.sksamuel.healthcheck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HealthCheckRegistry(private val healthchecks: List<Pair<String, HealthCheck>>) {

  fun register(name: String, healthCheck: HealthCheck): HealthCheckRegistry {
    return HealthCheckRegistry(healthchecks + (name to healthCheck))
  }

  suspend fun execute(dispatcher: CoroutineDispatcher): HealthCheckResponse {
    val results = coroutineScope {
      val jobs = healthchecks.map { (name, healthcheck) ->
        async(dispatcher) {
          try {
            healthcheck.check()
          } catch (t: Throwable) {
            HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
          }
        }
      }
      jobs.map { it.await() }
    }
    val status = if (results.any { it is HealthCheckResult.Unhealthy }) HealthStatus.Unhealthy else HealthStatus.Healthy
    return HealthCheckResponse(status, results)
  }
}

enum class HealthStatus {
  Healthy, Unhealthy
}

data class HealthCheckResponse(val status: HealthStatus, val results: List<HealthCheckResult>) {
  fun <A> fold(ifUnhealthy: (List<HealthCheckResult>) -> A, ifHealthy: (List<HealthCheckResult>) -> A): A {
    return when (status) {
      HealthStatus.Healthy -> ifHealthy(results)
      HealthStatus.Unhealthy -> ifUnhealthy(results)
    }
  }
}
