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
  // Cover dumpHeap with the same try/finally as the read; if dumpHeap partially writes the
  // file and then throws (disk full, IOException, JMX issue) the temp file would otherwise
  // be left behind on every failed call.
  try {
    mxBean.dumpHeap(path.toString(), true)
    Files.readAllBytes(path)
  } finally {
    path.deleteIfExists()
  }
}
