package com.sksamuel.cohort

import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.math.roundToInt

class AvailableDiskSpaceCheck(
  private val fileStore: FileStore,
  private val minFreeSpacePercentage: Double = 10.0
) : Check {

  override fun check(): CheckResult {
    return try {
      val availablePercent = (fileStore.usableSpace.toDouble() / fileStore.totalSpace.toDouble() * 100).roundToInt()
      if (availablePercent < minFreeSpacePercentage)
        CheckResult.Unhealthy("Available disk space is $availablePercent% on ${fileStore.name()}", null)
      else
        CheckResult.Healthy("Available disk space is $availablePercent% on ${fileStore.name()}")
    } catch (t: Throwable) {
      CheckResult.Unhealthy("Error querying disk space on ${fileStore.name()}", t)
    }
  }

  companion object {

    /**
     * Returns a [AvailableDiskSpaceCheck] for a single root directory for the default file system.
     * If there is more than one root dir, an error will be thrown.
     */
    fun root(minFreeSpacePercentage: Double = 10.0): AvailableDiskSpaceCheck {
      val root = FileSystems.getDefault().rootDirectories.single()
      return AvailableDiskSpaceCheck(Files.getFileStore(root), minFreeSpacePercentage)
    }

    fun defaults(minFreeSpacePercentage: Double = 10.0): List<Check> =
      FileSystems.getDefault().fileStores.map { AvailableDiskSpaceCheck(it, minFreeSpacePercentage) }
  }
}
