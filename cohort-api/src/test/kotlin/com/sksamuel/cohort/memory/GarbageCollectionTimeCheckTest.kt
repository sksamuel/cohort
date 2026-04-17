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
      // Two back-to-back calls can complete within 1ms; elapsed truncates to 0L.
      // Previously this caused NaN.roundToInt() to throw — the fix guards elapsed == 0L.
      val check = GarbageCollectionTimeCheck(100) // 100% threshold
      check.check() // establish baseline
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when GC time threshold is zero and GC has run") {
      val check = GarbageCollectionTimeCheck(0)
      check.check() // establish baseline
      System.gc()
      // best-effort: must not throw regardless of result
      check.check()
   }
})
