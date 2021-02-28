package com.sksamuel.healthcheck

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HealthCheckRegistry(private val healthchecks: List<HealthCheck>) {

  fun register(healthCheck: HealthCheck): HealthCheckRegistry {
    return HealthCheckRegistry(healthchecks + healthCheck)
  }

  suspend fun execute(dispatcher: CoroutineDispatcher): List<HealthCheckResult> {
    return coroutineScope {
      val jobs = healthchecks.map {
        async(dispatcher) {
          it.check()
        }
      }
      jobs.map { it.await() }
    }
  }
}
