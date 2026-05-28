package com.sksamuel.cohort

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class NamedThreadFactory(val name: String, private val daemon: Boolean = false) : ThreadFactory {
   // Per-instance counter so each factory numbers its threads from 0. The previous file-level
   // counter was shared across every NamedThreadFactory in the JVM, producing non-contiguous
   // thread names per factory (e.g. "scheduler-0", "scheduler-3", ...).
   private val counter = AtomicInteger(0)

   override fun newThread(r: Runnable): Thread {
      val t = Thread(r, name + "-" + counter.getAndIncrement())
      t.isDaemon = daemon
      t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e ->
         e.printStackTrace()
      }
      return t
   }
}
