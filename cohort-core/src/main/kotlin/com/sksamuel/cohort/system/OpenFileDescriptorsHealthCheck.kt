package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sun.management.UnixOperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * A Cohort [HealthCheck] for the number of open file descriptors.
 *
 * The check is considered healthy if the count of open descriptors <= [maxOpenFileDescriptors].
 */
class OpenFileDescriptorsHealthCheck(private val maxOpenFileDescriptors: Int) : HealthCheck {

  private val bean = ManagementFactory.getOperatingSystemMXBean() as UnixOperatingSystemMXBean

  override suspend fun check(): HealthCheckResult {
    val open = bean.openFileDescriptorCount
    return if (open < maxOpenFileDescriptors) {
      HealthCheckResult.Healthy("Open file descriptor count within threshold [$open <= $maxOpenFileDescriptors]")
    } else {
      HealthCheckResult.Unhealthy("Open file descriptors count above threshold [$open > $maxOpenFileDescriptors]", null)
    }
  }
}
