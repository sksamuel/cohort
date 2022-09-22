package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.Warmup
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class HttpWarmup(
   override val iterations: Int = 100,
   override val interval: Duration = 10.milliseconds,
   private val command: suspend (HttpClient) -> Unit,
) : Warmup() {

   private val logger = KotlinLogging.logger {}

   override val name: String = "http_warmup"

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
