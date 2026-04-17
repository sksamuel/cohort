package com.sksamuel.cohort.system

import com.sksamuel.cohort.HealthStatus
import com.sun.management.UnixOperatingSystemMXBean
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.lang.management.ManagementFactory

class MaxFileDescriptorsHealthCheckTest : FunSpec({

   val bean = ManagementFactory.getOperatingSystemMXBean() as UnixOperatingSystemMXBean

   test("returns healthy when system max fd meets required threshold") {
      MaxFileDescriptorsHealthCheck(1).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when required exceeds system max") {
      MaxFileDescriptorsHealthCheck(Int.MAX_VALUE).check().status shouldBe HealthStatus.Unhealthy
   }

   test("returns healthy when required exactly equals system max") {
      // verifies >= semantics: equal-to-required is healthy
      val actual = bean.maxFileDescriptorCount.toInt()
      MaxFileDescriptorsHealthCheck(actual).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when required is one above system max") {
      val actual = bean.maxFileDescriptorCount.toInt()
      MaxFileDescriptorsHealthCheck(actual + 1).check().status shouldBe HealthStatus.Unhealthy
   }
})
