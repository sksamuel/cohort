package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.Cohort
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HttpWarmupTest : FunSpec({

   test("HttpWarmup happy path") {

      val count = AtomicInteger(0)
      val client = HttpClient(Apache5)

      val server = embeddedServer(Netty, port = 11223) {
         routing {
            get("/foo") {
               call.respond(HttpStatusCode.OK)
            }
         }
         install(Cohort) {
            warmups {
               register(
                  HttpWarmup(client, 10.milliseconds) {
                     count.incrementAndGet()
                     client.get("http://localhost:11223/foo")
                  },
                  5.seconds
               )
            }
         }
      }.start(false)

      eventually(5.seconds) {
         count.get().shouldBeGreaterThan(100)
      }

      // let the warmup complete without spewing errors
      delay(5.seconds)

      // now warmup is done, we can stop server
      server.stop()
   }
})
