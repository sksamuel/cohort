package com.sksamuel.cohort.memory

import java.lang.management.BufferPoolMXBean
import java.lang.management.ManagementFactory

fun getMemoryInfo(): Result<MemoryInfo> = runCatching {
  val memoryPools = ManagementFactory.getMemoryPoolMXBeans()
  val bufferPools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean::class.java)
  MemoryInfo(memoryPools, bufferPools)
}
