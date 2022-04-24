package com.sksamuel.cohort.shutdown

import java.util.concurrent.atomic.AtomicBoolean

fun interface ShutdownHook {
  suspend fun run()
}

class AtomicShutdownHook(private val hook: ShutdownHook) : ShutdownHook {
  private val invoked = AtomicBoolean(false)
  override suspend fun run() {
    if (invoked.compareAndSet(false, true)) {
      hook.run()
    }
  }
}
