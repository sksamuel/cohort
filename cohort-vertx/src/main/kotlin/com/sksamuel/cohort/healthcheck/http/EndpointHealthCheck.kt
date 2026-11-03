package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse

/**
 * Executes a custom http request using a caller-supplied Vertx [HttpClient].
 *
 * The [HttpClient] is owned by the caller — its lifecycle (connection pool, netty event loops)
 * is the caller's responsibility. Previously this class created its own client via
 * `vertx.createHttpClient()` and never closed it, leaking pooled connections per instance.
 */
class EndpointHealthCheck(
   private val client: HttpClient,
   private val eval: suspend (HttpClientResponse) -> Boolean = { it.statusCode() in 200..299 },
   override val name: String = "endpoint_request",
   private val fn: suspend (HttpClient) -> HttpClientResponse,
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      val resp = fn(client)
      return if (eval(resp)) {
         HealthCheckResult.healthy("Endpoint returned ${resp.statusCode()}")
      } else
         HealthCheckResult.unhealthy("Endpoint returned ${resp.statusCode()}", null)
   }
}
