package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MaxFileDescriptorsHealthCheckTest : FunSpec({

   test("returns healthy when the system max meets the required minimum") {
      // Any real system will have more than 1 max file descriptor
      MaxFileDescriptorsHealthCheck(1).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when the system max is below the required minimum") {
      // Int.MAX_VALUE far exceeds any real OS limit
      MaxFileDescriptorsHealthCheck(Int.MAX_VALUE).check().status shouldBe HealthStatus.Unhealthy
   }
})
