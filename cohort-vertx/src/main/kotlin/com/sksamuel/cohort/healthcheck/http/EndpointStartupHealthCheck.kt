package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
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
   vertx: Vertx,
   override val name: String = "endpoint_startup_request",
   private val fn: suspend (HttpClient) -> Boolean,
) : HealthCheck {

   private val successful = AtomicBoolean(false)
   private val client = vertx.createHttpClient()

   override suspend fun check(): HealthCheckResult {
      return if (successful.get()) {
         HealthCheckResult.healthy("Service connection was successful")
      } else {
         val success = fn(client)
         if (success) {
            successful.set(true)
            HealthCheckResult.healthy("Service connection was successful")
         } else {
            HealthCheckResult.unhealthy("Service connection was unsuccessful", null)
         }
      }
   }
}
