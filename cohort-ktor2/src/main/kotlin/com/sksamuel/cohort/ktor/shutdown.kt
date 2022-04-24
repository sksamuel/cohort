package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.shutdown.ShutdownHook
import io.ktor.server.engine.ApplicationEngine
import kotlin.time.Duration

class EngineShutdownHook(
  private val gracePeriod: Duration,
  private val timeout: Duration
) : ShutdownHook {

  private var engine: ApplicationEngine? = null

  fun setEngine(engine: ApplicationEngine) {
    this.engine = engine
  }

  override suspend fun run() {
    engine?.stop(gracePeriod.inWholeMilliseconds, timeout.inWholeMilliseconds)
  }
}
