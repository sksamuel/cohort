package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthStatus
import com.sun.management.UnixOperatingSystemMXBean
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.lang.management.ManagementFactory

class OpenFileDescriptorsHealthCheckTest : FunSpec({

   val bean = ManagementFactory.getOperatingSystemMXBean() as UnixOperatingSystemMXBean

   test("returns healthy when open count is below threshold") {
      OpenFileDescriptorsHealthCheck(Int.MAX_VALUE).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when threshold is zero") {
      // open fd count is always > 0 while tests run
      OpenFileDescriptorsHealthCheck(0).check().status shouldBe HealthStatus.Unhealthy
   }

   test("returns healthy when open count exactly equals threshold") {
      // verifies <= semantics: equal-to-threshold is healthy, not unhealthy
      val actual = bean.openFileDescriptorCount.toInt()
      OpenFileDescriptorsHealthCheck(actual).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when threshold is one below current open count") {
      val actual = bean.openFileDescriptorCount.toInt()
      OpenFileDescriptorsHealthCheck(actual - 1).check().status shouldBe HealthStatus.Unhealthy
   }
})
