package com.sksamuel.cohort.threads

import java.lang.management.ManagementFactory

fun getThreadDump(): Result<String> = runCatching {
  // Use StringBuilder (unsynchronized) and append a line separator between threads so the
  // dump is readable; ThreadInfo.toString() does not include a trailing newline.
  val threadDump = StringBuilder()
  val threadMXBean = ManagementFactory.getThreadMXBean()
  threadMXBean.dumpAllThreads(true, true).forEach { t ->
    threadDump.append(t.toString()).append(System.lineSeparator())
  }
  threadDump.toString()
}
