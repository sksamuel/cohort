package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import java.lang.management.BufferPoolMXBean
import java.lang.management.MemoryPoolMXBean

/**
 * A Cohort [HealthCheck] that checks free memory in the system.
 *
 * The check is considered healthy if the amount of free memory is above [minFreeBytes].
 */
class FreememHealthCheck(private val minFreeBytes: Int) : HealthCheck {

  companion object {
    fun mb(mb: Int) = FreememHealthCheck(mb * 1024 * 1024)
    fun gb(gb: Int) = FreememHealthCheck(gb * 1024 * 1024 * 1024)
  }

  override suspend fun check(): HealthCheckResult {
    val free = Runtime.getRuntime().freeMemory()
    val msg = "Freemem $free bytes [min free $minFreeBytes]"
    return if (free < minFreeBytes) {
      HealthCheckResult.Unhealthy(msg, null)
    } else {
      HealthCheckResult.Healthy(msg)
    }
  }
}


data class MemoryInfo(val memoryPools: List<MemoryPoolMXBean>, val bufferPools: List<BufferPoolMXBean>)
