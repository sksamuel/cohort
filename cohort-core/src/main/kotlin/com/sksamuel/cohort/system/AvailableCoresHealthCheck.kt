package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] for the number of available CPU cores.
 *
 * While the number of cores won't change during the lifetime of a pod, this check can be useful to avoid
 * accidentally deploying pods into environments that don't have the required resources.
 *
 * The check is considered healthy if the cpu core count is >= [minCores].
 */
class AvailableCoresHealthCheck(private val minCores: Double) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    val cores = Runtime.getRuntime().availableProcessors()
    return if (cores < minCores) {
      HealthCheckResult.Unhealthy("Available CPU cores are below threshold [$cores < $minCores]", null)
    } else {
      HealthCheckResult.Healthy("Available CPU cores are above threshold [$cores >= $minCores]")
    }
  }

}
