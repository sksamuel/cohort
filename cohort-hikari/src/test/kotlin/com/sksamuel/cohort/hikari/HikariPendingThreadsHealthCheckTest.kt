package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HikariPendingThreadsHealthCheckTest : FunSpec({

   test("returns healthy when awaiting threads is within the threshold") {
      // An idle pool has 0 threads awaiting; 0 <= 0 is within threshold
      HikariPendingThreadsHealthCheck(createHikariDS(), maxAwaiting = 0).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when awaiting threads exceeds the threshold") {
      // maxAwaiting = -1 means any waiting count is too many; 0 > -1 → unhealthy
      HikariPendingThreadsHealthCheck(createHikariDS(), maxAwaiting = -1).check().status shouldBe HealthStatus.Unhealthy
   }
})
