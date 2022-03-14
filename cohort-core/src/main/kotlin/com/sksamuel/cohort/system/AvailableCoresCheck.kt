package com.sksamuel.cohort.system

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult

/**
 * A Cohort [Check] for the number of available CPU cores.
 *
 * While the number of cores won't change during the lifetime of a pod, this check can be useful to avoid
 * accidentally deploying pods into environments that don't have the required resources.
 *
 * The check is considered healthy if the cpu core count is >= [minCores].
 */
class AvailableCoresCheck(private val minCores: Double) : Check {

  override suspend fun check(): CheckResult {
    val cores = Runtime.getRuntime().availableProcessors()
    return if (cores < minCores) {
      CheckResult.Unhealthy("Available CPU cores are below threshold [$cores < $minCores]", null)
    } else {
      CheckResult.Healthy("Available CPU cores are above threshold [$cores >= $minCores]")
    }
  }

}
