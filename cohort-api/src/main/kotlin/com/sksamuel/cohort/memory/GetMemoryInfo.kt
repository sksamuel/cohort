package com.sksamuel.cohort.memory

import java.lang.management.BufferPoolMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage as JavaMemoryUsage

fun getMemoryInfo(): Result<MemoryInfo> = runCatching {
  val memoryPools = ManagementFactory.getMemoryPoolMXBeans().map { it.toMemoryPool() }
  val bufferPools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean::class.java).map { it.toBufferPool() }
  MemoryInfo(memoryPools, bufferPools)
}

private fun MemoryPoolMXBean.toMemoryPool(): MemoryPool = MemoryPool(
  name = name,
  type = type.name,
  usage = runCatching { usage }.getOrNull()?.toMemoryUsage(),
  peakUsage = runCatching { peakUsage }.getOrNull()?.toMemoryUsage(),
  collectionUsage = runCatching { collectionUsage }.getOrNull()?.toMemoryUsage(),
  memoryManagerNames = memoryManagerNames.toList(),
  valid = isValid,
)

private fun BufferPoolMXBean.toBufferPool(): BufferPool = BufferPool(
  name = name,
  count = count,
  memoryUsed = memoryUsed,
  totalCapacity = totalCapacity,
)

private fun JavaMemoryUsage.toMemoryUsage(): MemoryUsage = MemoryUsage(
  init = init,
  used = used,
  committed = committed,
  max = max,
)
