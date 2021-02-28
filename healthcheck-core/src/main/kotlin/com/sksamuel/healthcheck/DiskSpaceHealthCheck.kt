package com.sksamuel.healthcheck

import java.nio.file.FileStore
import java.nio.file.FileSystems

class DiskSpaceHealthCheck(
  private val fileStore: FileStore,
  private val minFreeSpacePercentage: Double = 10.0
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return try {
      val availablePercent = fileStore.usableSpace / fileStore.totalSpace
      if (availablePercent < minFreeSpacePercentage)
        HealthCheckResult.Unhealthy("Disk space has dropped to $availablePercent on ${fileStore.name()}", null)
      else
        HealthCheckResult.Healthy("Disk space is $availablePercent on ${fileStore.name()}")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error querying disk space on ${fileStore.name()}", t)
    }
  }

  companion object {

    /**
     * For systems which only have one file system, eg unix, this will return a [DiskSpaceHealthCheck]
     * for that file system. If there is more than one, an error will be thrown.
     */
    fun default(minFreeSpacePercentage: Double = 10.0): DiskSpaceHealthCheck {
      val filestore = FileSystems.getDefault().fileStores.single()
      return DiskSpaceHealthCheck(filestore, minFreeSpacePercentage)
    }

    fun defaults(minFreeSpacePercentage: Double = 10.0): List<HealthCheck> =
      FileSystems.getDefault().fileStores.map { DiskSpaceHealthCheck(it, minFreeSpacePercentage) }
  }
}
