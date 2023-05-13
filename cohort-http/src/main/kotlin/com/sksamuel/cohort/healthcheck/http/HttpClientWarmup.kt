package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.WarmupHealthCheck
import io.ktor.client.HttpClient

/**
 * A Cohort [HttpClientWarmup] that executes requests against a supplied Ktor [client].
 */
class HttpClientWarmup(
   private val client: HttpClient,
   override val iterations: Int,
   private val eval: suspend (HttpClient) -> Unit
) : WarmupHealthCheck() {

   override val name = "http_client_warmup"

   override suspend fun warm(iteration: Int) {
      eval(client)
   }
}
