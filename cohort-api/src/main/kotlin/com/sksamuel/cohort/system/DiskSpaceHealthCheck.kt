package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * A Cohort [HealthCheck] that examines disk space on the given [FileStore].
 *
 * A file store represents a device, partition, volume, and so on.
 * A filesystem may have multiple file stores.
 *
 * For example, on linux, the default file system would typically have /, /sys, /proc and so on.
 *
 * To configure a check for the file store for the root directory, use [DiskSpaceHealthCheck.root].
 *
 * The check is considered healthy if the percentage of free space is above [minFreeSpacePercentage].
 */
class DiskSpaceHealthCheck(
  private val fileStore: FileStore,
  private val minFreeSpacePercentage: Double = 10.0,
  override val name: String = "disk_space_free",
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return try {
      // Compare on the raw Double so values just below the threshold (e.g. 9.5% with a
      // 10.0% threshold) aren't rounded up and reported as healthy.
      val availablePercent = fileStore.usableSpace.toDouble() / fileStore.totalSpace.toDouble() * 100
      val msg = "Available disk space is %.2f%% on %s".format(availablePercent, fileStore.name())
      if (availablePercent < minFreeSpacePercentage)
        HealthCheckResult.unhealthy(msg, null)
      else
        HealthCheckResult.healthy(msg)
    } catch (t: Throwable) {
      HealthCheckResult.unhealthy("Error querying disk space on ${fileStore.name()}", t)
    }
  }

  companion object {

    /**
     * Returns a [DiskSpaceHealthCheck] for the root file store on the default file system.
     * If there is more than one root directory available, an error will be thrown.
     */
    fun root(minFreeSpacePercentage: Double = 10.0): DiskSpaceHealthCheck {
      val root = FileSystems.getDefault().rootDirectories.single()
      return DiskSpaceHealthCheck(Files.getFileStore(root), minFreeSpacePercentage)
    }

    /**
     * Returns a [DiskSpaceHealthCheck] for each filestore in the get file system.
     */
    fun defaults(minFreeSpacePercentage: Double = 10.0): List<HealthCheck> =
      FileSystems.getDefault().fileStores.map { DiskSpaceHealthCheck(it, minFreeSpacePercentage) }
  }
}
