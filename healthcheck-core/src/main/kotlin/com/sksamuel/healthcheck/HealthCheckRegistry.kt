package com.sksamuel.healthcheck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HealthCheckRegistry(private val healthchecks: List<HealthCheck>) {

  fun register(healthCheck: HealthCheck): HealthCheckRegistry {
    return HealthCheckRegistry(healthchecks + healthCheck)
  }

  suspend fun execute(dispatcher: CoroutineDispatcher): HealthCheckResponse {
    val results = coroutineScope {
      val jobs = healthchecks.map {
        async(dispatcher) {
          it.check()
        }
      }
      jobs.map { it.await() }
    }
    val status = if (results.any { it is HealthCheckResult.Unhealthy }) HealthStatus.Green else HealthStatus.Red
    return HealthCheckResponse(status, results)
  }
}

enum class HealthStatus {
  Green, Red
}

data class HealthCheckResponse(val status: HealthStatus, val results: List<HealthCheckResult>) {
  fun <A> fold(ifRed: (List<HealthCheckResult>) -> A, ifGreen: (List<HealthCheckResult>) -> A): A {
    return when (status) {
      HealthStatus.Green -> ifGreen(results)
      HealthStatus.Red -> ifRed(results)
    }
  }
}
