package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.shutdown.ShutdownHook
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.delay
import kotlin.time.Duration

/**
 * Shutsdown the Ktor [ApplicationEngine].
 *
 * @param prewait a duration to wait before beginning the stop process. During this time, requests will continue
 * to be accepted. This setting is useful to allow time for the container to be removed from the load balancer.
 * @param gracePeriod a duration during which already inflight requests are allowed to continue.
 * @param timeout a duration after which the server will be forceably shutdown.
 */
class EngineShutdownHook(
  private val prewait: Duration,
  private val gracePeriod: Duration,
  private val timeout: Duration,
) : ShutdownHook {

  private var engine: ApplicationEngine? = null

  fun setEngine(engine: ApplicationEngine) {
    this.engine = engine
  }

  override suspend fun run() {
    delay(prewait)
    engine?.application?.environment?.log?.info("Shutting down HTTP server...")
    engine?.stop(gracePeriod.inWholeMilliseconds, timeout.inWholeMilliseconds)
    engine?.application?.environment?.log?.info("HTTP server shutdown!")
  }
}
