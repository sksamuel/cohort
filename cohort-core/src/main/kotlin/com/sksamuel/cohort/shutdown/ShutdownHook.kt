package com.sksamuel.cohort.shutdown

import java.util.concurrent.atomic.AtomicBoolean

class ShutdownHook(private val f: suspend () -> Unit) {
  private val invoked = AtomicBoolean(false)
  suspend fun run() {
    if (invoked.compareAndSet(false, true)) {
      f()
    }
  }
}
