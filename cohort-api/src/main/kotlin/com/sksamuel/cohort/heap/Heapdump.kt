package com.sksamuel.cohort.heap

import com.sun.management.HotSpotDiagnosticMXBean
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.deleteIfExists

private const val HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic"

fun getHeapDump(): Result<ByteArray> = runCatching {
  val server = ManagementFactory.getPlatformMBeanServer()
  val mxBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean::class.java)
  val tmpDir = Paths.get(System.getProperty("java.io.tmpdir"))
  // dumpHeap throws if the target already exists, so use a UUID rather than currentTimeMillis
  // (two calls in the same millisecond would otherwise collide).
  val path = tmpDir.resolve("heapdump-${UUID.randomUUID()}.hprof")
  mxBean.dumpHeap(path.toString(), true)
  try {
    Files.readAllBytes(path)
  } finally {
    path.deleteIfExists()
  }
}
