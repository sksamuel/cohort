package com.sksamuel.cohort.threads

import java.lang.management.ManagementFactory

fun getThreadDump() = runCatching {
  val threadDump = StringBuffer(System.lineSeparator())
  val threadMXBean = ManagementFactory.getThreadMXBean()
  threadMXBean.dumpAllThreads(true, true).forEach { t ->
    threadDump.append(t.toString())
  }
  threadDump.toString()
}
