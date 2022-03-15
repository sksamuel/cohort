package com.sksamuel.cohort.jvm

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] that checks how many classes have been loaded in the JVM.
 *
 * The check is considered healthy if the number of loaded classes is equal to or below [maxLoadedClasses].
 */
class LoadedClassesHealthCheck(private val maxLoadedClasses: Int) : HealthCheck {

  private val bean = ManagementFactory.getClassLoadingMXBean()

  override suspend fun check(): HealthCheckResult {
    val count = bean.loadedClassCount
    return if (count <= maxLoadedClasses) {
      HealthCheckResult.Healthy("Loaded classes are within threshold [$count <= $maxLoadedClasses]")
    } else {
      HealthCheckResult.Unhealthy("Loaded classes are above threshold [$count > $maxLoadedClasses]", null)
    }
  }

}
