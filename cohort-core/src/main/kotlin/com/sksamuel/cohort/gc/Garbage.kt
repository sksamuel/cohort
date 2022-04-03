package com.sksamuel.cohort.gc

import java.lang.management.ManagementFactory

fun gcinfo(): Result<List<GarbageCollector>> = runCatching {
  ManagementFactory.getGarbageCollectorMXBeans().map {
    GarbageCollector(
      it.name,
      it.collectionTime,
      it.collectionCount,
    )
  }
}

data class GarbageCollector(
  val name: String,
  val collectionTime: Long,
  val collectionCount: Long,
)
