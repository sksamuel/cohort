package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.endpoints.cohort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class EndpointStartupHealthCheckTest : FunSpec({

   test("EndpointStartupHealthCheck should only hit the service until successful") {

      val count = AtomicInteger(0)
      val client = HttpClient(Apache5)

      val server = embeddedServer(Netty, port = 11224) {
         routing {
            get("/foo") {
               count.incrementAndGet()
               call.respond(HttpStatusCode.OK)
            }
            cohort {
               healthcheck("/healthy", HealthCheckRegistry {
                  register(EndpointStartupHealthCheck(client) {
                     client.get("http://localhost:11224/foo").status == HttpStatusCode.OK
                  }, 100.milliseconds)
               })
            }
         }
      }.start(false)

      delay(2.seconds) // let check run a few times
      count.get() shouldBe 1
      server.stop()
   }
})
