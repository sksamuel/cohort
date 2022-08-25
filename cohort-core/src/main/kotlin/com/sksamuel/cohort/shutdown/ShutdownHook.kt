package com.sksamuel.cohort.shutdown

import java.util.concurrent.atomic.AtomicBoolean

@Deprecated("Can use shutdown hook on ktor directly")
fun interface ShutdownHook {
  suspend fun run()
}

@Deprecated("Can use shutdown hook on ktor directly")
class AtomicShutdownHook(private val hook: ShutdownHook) : ShutdownHook {
  private val invoked = AtomicBoolean(false)
  override suspend fun run() {
    if (invoked.compareAndSet(false, true)) {
      hook.run()
    }
  }
}
