package com.sksamuel.cohort.gc

import java.lang.management.ManagementFactory

fun getGcInfo(): Result<GCInfo> = runCatching {
  GCInfo(
    ManagementFactory.getGarbageCollectorMXBeans().map {
      GarbageCollector(
        it.name,
        it.collectionTime,
        it.collectionCount,
      )
    })
}

data class GCInfo(
  val collectors: List<GarbageCollector>,
)

data class GarbageCollector(
  val name: String,
  val collectionTime: Long,
  val collectionCount: Long,
)
