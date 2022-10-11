package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.Warmup
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache

class HttpWarmup(
   private val command: suspend (HttpClient) -> Unit,
) : Warmup {

   override val name: String = "http_warmup"

   private val client = HttpClient(Apache) {
      expectSuccess = false
   }

   override suspend fun warm(iteration: Int) {
      command(client)
   }

   override suspend fun close() {
      client.close()
   }
}
