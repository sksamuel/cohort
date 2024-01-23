package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.ktor.client.HttpClient
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A Cohort [HealthCheck] which will attempt to connect to a given endpoint. Once successful,
 * this health check will then return healthy indefinitely with no further connection attempts.
 *
 * The purpose of this healthcheck is to test connectivity to another service on startup as a form
 * of smoke test, to ensure configuration is correct.
 *
 * It does not continually ping a service, to avoid an upstream service going down, and bringing
 * down all the dependent services with it in a cascading fashion.
 */
class EndpointStartupHealthCheck(
   private val client: HttpClient,
   override val name: String = "endpoint_startup_request",
   private val eval: suspend (HttpClient) -> Boolean,
) : HealthCheck {

   private val successful = AtomicBoolean(false)

   override suspend fun check(): HealthCheckResult {
      return if (successful.get()) {
         HealthCheckResult.healthy("Service connection was successful")
      } else {
         val success = eval(client)
         if (success) {
            successful.set(true)
            HealthCheckResult.healthy("Service connection was successful")
         } else {
            HealthCheckResult.unhealthy("Service connection was unsuccessful", null)
         }
      }
   }
}
