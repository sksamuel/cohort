package com.sksamuel.cohort

import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.math.roundToInt

fun main() {
  FileSystems.getDefault().rootDirectories.forEach { println(it) }
}

/**
 * A Cohort [Check] that examines disk space on the given [FileStore].
 *
 * A file store represents a device, partition, volume, and so on.
 * A filesystem may have multiple file stores.
 *
 * For example, on linux, the default file system would typically have /, /sys, /proc and so on.
 *
 * To configure a check for the file store for the root directory, use [AvailableDiskSpaceCheck.root].
 *
 * The check is considered healthy if the percentage of free space is above [minFreeSpacePercentage].
 */
class AvailableDiskSpaceCheck(
  private val fileStore: FileStore,
  private val minFreeSpacePercentage: Double = 10.0
) : Check {

  override suspend fun check(): CheckResult {
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
     * Returns a [AvailableDiskSpaceCheck] for the root file store on the default file system.
     * If there is more than one root directory available, an error will be thrown.
     */
    fun root(minFreeSpacePercentage: Double = 10.0): AvailableDiskSpaceCheck {
      val root = FileSystems.getDefault().rootDirectories.single()
      return AvailableDiskSpaceCheck(Files.getFileStore(root), minFreeSpacePercentage)
    }

    /**
     * Returns a [AvailableDiskSpaceCheck] for each filestore in the get file system.
     */
    fun defaults(minFreeSpacePercentage: Double = 10.0): List<Check> =
      FileSystems.getDefault().fileStores.map { AvailableDiskSpaceCheck(it, minFreeSpacePercentage) }
  }
}
