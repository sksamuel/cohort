package com.sksamuel.healthcheck

import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.math.roundToInt

class DiskSpaceHealthCheck(
  private val fileStore: FileStore,
  private val minFreeSpacePercentage: Double = 10.0
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return try {
      val availablePercent = (fileStore.usableSpace.toDouble() / fileStore.totalSpace.toDouble() * 100).roundToInt()
      if (availablePercent < minFreeSpacePercentage)
        HealthCheckResult.Unhealthy("Disk space is $availablePercent% on ${fileStore.name()}", null)
      else
        HealthCheckResult.Healthy("Disk space is $availablePercent% on ${fileStore.name()}")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error querying disk space on ${fileStore.name()}", t)
    }
  }

  companion object {

    /**
     * Returns a [DiskSpaceHealthCheck] for a single root directory for the default file system.
     * If there is more than one root dir, an error will be thrown.
     */
    fun root(minFreeSpacePercentage: Double = 10.0): DiskSpaceHealthCheck {
      val root = FileSystems.getDefault().rootDirectories.single()
      return DiskSpaceHealthCheck(Files.getFileStore(root), minFreeSpacePercentage)
    }

    fun defaults(minFreeSpacePercentage: Double = 10.0): List<HealthCheck> =
      FileSystems.getDefault().fileStores.map { DiskSpaceHealthCheck(it, minFreeSpacePercentage) }
  }
}
