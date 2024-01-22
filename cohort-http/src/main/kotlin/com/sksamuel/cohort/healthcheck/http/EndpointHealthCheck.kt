package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Executes a custom http request using a Ktor [Apache5] client.
 */
class EndpointHealthCheck(
   private val eval: suspend (HttpResponse) -> Boolean = { it.status.isSuccess() },
   override val name: String = "endpoint_request",
   private val fn: suspend (HttpClient) -> HttpResponse,
) : HealthCheck {

   private val client = HttpClient(Apache5) {
      expectSuccess = false
   }

   override suspend fun check(): HealthCheckResult {
      val resp = fn(client)
      return if (eval(resp)) {
         HealthCheckResult.healthy("Endpoint returned ${resp.status}")
      } else
         HealthCheckResult.unhealthy("Endpoint returned ${resp.status}", null)
   }
}
