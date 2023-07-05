package com.sksamuel.cohort

import com.sksamuel.cohort.cpu.CryptoWarmup
import com.sksamuel.cohort.endpoints.cohort
import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class WarmupTest : FunSpec() {
   init {
      test("health check should return unhealthy until the warmups have completed") {

         val registry = HealthCheckRegistry {
            register(ThreadDeadlockHealthCheck(), delay = 10.milliseconds)
            startUnhealthy = true
         }

         testApplication {
            routing {
               cohort {
                  warmup {
                     register(CryptoWarmup(), 2.seconds)
                  }
                  healthcheck("healthy-mchealth-face", registry)
               }
            }

            client.get("/healthy-mchealth-face").status shouldBe HttpStatusCode.ServiceUnavailable
            delay(4.seconds)
            client.get("/healthy-mchealth-face").status shouldBe HttpStatusCode.OK
         }
      }
   }
}
