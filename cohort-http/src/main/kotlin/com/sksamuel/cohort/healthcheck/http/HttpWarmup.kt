package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.WarmupHealthCheck
import com.sksamuel.cohort.cpu.FibWarmup
import com.sksamuel.cohort.cpu.HotSpotCompilationTimeHealthCheck
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.lang.management.ManagementFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class HttpWarmup(
   override val iterations: Int = 100,
   override val interval: Duration = 10.milliseconds,
   private val command: suspend (HttpClient) -> Unit,
) : WarmupHealthCheck() {

   private val logger = KotlinLogging.logger {}

   private val client = HttpClient(Apache) {
      expectSuccess = false
   }

   override suspend fun warmup() {
      runCatching {
         command(client)
      }.onFailure { logger.warn(it) { "Error executing HTTP warmup call" } }
   }

   override fun close() {
      client.close()
   }
}

suspend fun main() {
   ManagementFactory.getClassLoadingMXBean().isVerbose = true
   val jackson = HttpWarmup() { it.get("https://www.google.com") }
   val hotspot = HotSpotCompilationTimeHealthCheck(2000)
   val fib = FibWarmup()
   val scope = CoroutineScope(Dispatchers.IO)
   jackson.start(scope)
   fib.start(scope)
   while (true) {
      delay(500)
      println(ManagementFactory.getCompilationMXBean().totalCompilationTime)
      println(ManagementFactory.getClassLoadingMXBean().loadedClassCount)
      println(jackson.check())
      println(hotspot.check())
      println(fib.check())
   }
}
