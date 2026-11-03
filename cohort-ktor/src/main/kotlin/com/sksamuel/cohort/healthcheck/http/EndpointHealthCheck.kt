package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Executes a custom http request using a caller-supplied Ktor [HttpClient].
 *
 * The [HttpClient] is owned by the caller — its lifecycle (connection pool, engine threads) is
 * the caller's responsibility. This mirrors [EndpointStartupHealthCheck], which has always
 * accepted the client externally.
 */
class EndpointHealthCheck(
   private val client: HttpClient,
   private val eval: suspend (HttpResponse) -> Boolean = { it.status.isSuccess() },
   override val name: String = "endpoint_request",
   private val fn: suspend (HttpClient) -> HttpResponse,
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      val resp = fn(client)
      return if (eval(resp)) {
         HealthCheckResult.healthy("Endpoint returned ${resp.status}")
      } else
         HealthCheckResult.unhealthy("Endpoint returned ${resp.status}", null)
   }
}
