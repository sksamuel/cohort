package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.WarmupHealthCheck
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5

class HttpWarmup(
   private val command: suspend (HttpClient) -> Unit,
   override val iterations: Int = 1000,
) : WarmupHealthCheck() {

   override val name: String = "http_warmup"

   private val client = HttpClient(Apache5) {
      expectSuccess = false
   }

   override suspend fun warm(iteration: Int) {
      command(client)
   }

   override suspend fun close() {
      client.close()
   }
}
