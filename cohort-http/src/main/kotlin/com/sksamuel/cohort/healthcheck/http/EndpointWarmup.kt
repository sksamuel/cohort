package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.WarmupHealthCheck
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.statement.HttpResponse

/**
 * A Cohort [WarmupHealthCheck] that executes a http request for the specified iteration count.
 */
class EndpointWarmup(
   override val iterations: Int = 1000,
   private val fn: suspend (HttpClient) -> HttpResponse,
) : WarmupHealthCheck() {

   override val name: String = "endpoint_warmup"

   private val client = HttpClient(Apache5) {
      expectSuccess = false
   }

   override suspend fun warm(iteration: Int) {
      fn(client)
   }
}
