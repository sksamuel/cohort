package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse

/**
 * Executes a custom http request using a Vertx HTTP client.
 * The result is considered healthy if [eval] returns true, which by default looks for a 2xx status code.
 */
class EndpointHealthCheck(
   vertx: Vertx,
   private val eval: suspend (HttpClientResponse) -> Boolean = { it.statusCode() in 200..299 },
   override val name: String = "endpoint_request",
   private val fn: suspend (HttpClient) -> HttpClientResponse,
) : HealthCheck {


   private val client = vertx.createHttpClient()

   override suspend fun check(): HealthCheckResult {
      val resp = fn(client)
      return if (eval(resp)) {
         HealthCheckResult.healthy("Endpoint returned ${resp.statusCode()}")
      } else
         HealthCheckResult.unhealthy("Endpoint returned ${resp.statusCode()}", null)
   }
}
