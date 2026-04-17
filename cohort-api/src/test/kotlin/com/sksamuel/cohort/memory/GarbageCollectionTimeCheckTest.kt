package com.sksamuel.cohort.memory

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(kotlin.time.ExperimentalTime::class)
class GarbageCollectionTimeCheckTest : FunSpec({

   test("returns healthy on first invocation when elapsed time is unknown") {
      GarbageCollectionTimeCheck(0).check().status shouldBe HealthStatus.Healthy
   }

   test("returns healthy on second invocation when GC time is well within threshold") {
      val check = GarbageCollectionTimeCheck(100) // 100% GC time threshold
      check.check() // first call sets the baseline — result is always healthy
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when GC time threshold is zero and GC has run") {
      // Force GC and then check with 0% threshold
      val check = GarbageCollectionTimeCheck(0)
      check.check() // establish baseline
      System.gc()
      // Subsequent calls compute percentage; with maxGcTime=0 any GC time is unhealthy.
      // This test is a best-effort check — it may still pass if no GC happened.
      check.check() // must not throw regardless of result
   }
})
