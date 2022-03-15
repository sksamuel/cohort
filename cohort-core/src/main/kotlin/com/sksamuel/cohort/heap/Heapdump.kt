package com.sksamuel.cohort.heap

import com.sun.management.HotSpotDiagnosticMXBean
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

private const val HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic"

fun getHeapDump(): Result<String> = runCatching {
  val server = ManagementFactory.getPlatformMBeanServer()
  val mxBean = ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean::class.java)
  val path = Paths.get("/tmp/heapdump" + System.currentTimeMillis() + ".hprof")
  mxBean.dumpHeap(path.toString(), true)
  val dump = Files.readString(path)
  path.deleteIfExists()
  dump
}
