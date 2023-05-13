package com.sksamuel.cohort.healthcheck.http

import io.kotest.assertions.timing.eventually
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
import kotlin.time.Duration.Companion.seconds

class EndpointWarmupTest : FunSpec() {

   @Volatile
   var count = 0

   init {
      test("testing success") {

         val server = embeddedServer(Netty, port = 1234) {
            routing {
               get("foo") {
                  count++
                  call.respond(HttpStatusCode.OK)
               }
            }
         }
         server.start(false)

         val check = EndpointWarmup { client ->
            client.get("http://localhost:1234/foo")
         }
         check.check()

         eventually(10.seconds) {
            count shouldBe 1000
         }

         server.stop()
      }
   }
}
