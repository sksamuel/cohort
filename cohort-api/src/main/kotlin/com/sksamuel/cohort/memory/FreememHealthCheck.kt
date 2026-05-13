package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks free memory in the system.
 *
 * The check is considered healthy if the amount of free memory is at or above [minFreeBytes].
 */
class FreememHealthCheck(private val minFreeBytes: Long) : HealthCheck {

  companion object {
    fun mb(mb: Int) = FreememHealthCheck(mb.toLong() * 1024L * 1024L)
    fun gb(gb: Int) = FreememHealthCheck(gb.toLong() * 1024L * 1024L * 1024L)
  }

  override val name: String = "free_mem"

  override suspend fun check(): HealthCheckResult {
    val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    val freeMemory = Runtime.getRuntime().maxMemory() - usedMemory
    val msg = "Freemem $freeMemory bytes [min free $minFreeBytes]"
    return if (freeMemory < minFreeBytes) {
      HealthCheckResult.unhealthy(msg, null)
    } else {
      HealthCheckResult.healthy(msg)
    }
  }
}

data class MemoryInfo(
  val memoryPools: List<MemoryPool>,
  val bufferPools: List<BufferPool>,
)

data class MemoryPool(
  val name: String,
  val type: String,
  val usage: MemoryUsage?,
  val peakUsage: MemoryUsage?,
  val collectionUsage: MemoryUsage?,
  val memoryManagerNames: List<String>,
  val valid: Boolean,
)

data class BufferPool(
  val name: String,
  val count: Long,
  val memoryUsed: Long,
  val totalCapacity: Long,
)

data class MemoryUsage(
  val init: Long,
  val used: Long,
  val committed: Long,
  val max: Long,
)
