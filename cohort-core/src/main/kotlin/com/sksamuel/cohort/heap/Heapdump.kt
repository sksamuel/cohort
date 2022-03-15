package com.sksamuel.cohort.heap

import com.sun.management.HotSpotDiagnosticMXBean
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists

object Heapdump {

  fun run(live: Boolean): String {
    val server = ManagementFactory.getPlatformMBeanServer()
    val mxBean = ManagementFactory.newPlatformMXBeanProxy(
      server,
      "com.sun.management:type=HotSpotDiagnostic",
      HotSpotDiagnosticMXBean::class.java,
    )
    val path = Paths.get("/tmp/heapdump" + System.currentTimeMillis() + ".hprof")
    mxBean.dumpHeap(path.toString(), live)
    val dump = Files.readString(path)
    path.deleteIfExists()
    return dump
  }
}
