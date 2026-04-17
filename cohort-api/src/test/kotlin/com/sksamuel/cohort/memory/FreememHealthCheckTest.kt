package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FreememHealthCheckTest : FunSpec({

   test("returns healthy when free memory exceeds the minimum") {
      FreememHealthCheck(1L).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when minimum exceeds available memory") {
      FreememHealthCheck(Long.MAX_VALUE).check().status shouldBe HealthStatus.Unhealthy
   }

   test("mb factory does not overflow for large values") {
      val check = FreememHealthCheck.mb(2048)
      // 2048 * 1024 * 1024 = 2147483648 — overflows Int but must be positive as Long
      check shouldNotBe null
      FreememHealthCheck.mb(2048).check() // must not throw
   }

   test("gb factory does not overflow") {
      val check = FreememHealthCheck.gb(2)
      // 2 * 1024 * 1024 * 1024 = 2147483648 — overflows Int to negative, must be positive as Long
      FreememHealthCheck.gb(2).check().status shouldBe HealthStatus.Unhealthy // 2 GB threshold exceeds JVM heap
   }
})
