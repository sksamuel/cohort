package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sun.management.UnixOperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] for the number of max file descriptors.
 *
 * The check is considered healthy if the max count is <= [requiredMaxDescriptors].
 */
class MaxFileDescriptorsHealthCheck(private val requiredMaxDescriptors: Int) : HealthCheck {

  private val bean = ManagementFactory.getOperatingSystemMXBean() as UnixOperatingSystemMXBean

  override suspend fun check(): HealthCheckResult {
    val max = bean.maxFileDescriptorCount
    val msg = "Max file descriptors $max [required at least $requiredMaxDescriptors]"
    return if (max < requiredMaxDescriptors) {
      HealthCheckResult.Healthy(msg)
    } else {
      HealthCheckResult.Unhealthy(msg, null)
    }
  }
}
