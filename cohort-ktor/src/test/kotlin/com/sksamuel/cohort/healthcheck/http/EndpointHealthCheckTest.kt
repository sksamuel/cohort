package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class EndpointHealthCheckTest : FunSpec() {
   init {

      val server = embeddedServer(Netty, port = 1234) {
         routing {
            get("foo") {
               call.respond(HttpStatusCode.OK)
            }
         }
      }
      server.start(false)

      afterSpec { server.stop() }

      test("testing fail") {
         val check = EndpointHealthCheck { client ->
            client.get("http://localhost:1234/bar")
         }
         check.check().status shouldBe HealthStatus.Unhealthy
      }

      test("testing success") {
         val check = EndpointHealthCheck { client ->
            client.get("http://localhost:1234/foo")
         }
         check.check().status shouldBe HealthStatus.Healthy
      }
   }
}
