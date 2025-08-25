package com.sksamuel.cohort

import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HealthCheckRegistryTest : FunSpec({

   test("duplicate name should throw error") {
      shouldThrowAny {
         HealthCheckRegistry {
            register(ThreadDeadlockHealthCheck())
            register(ThreadDeadlockHealthCheck())
         }
      }
   }

   class SlowHealthCheck(
      val blocker: AtomicBoolean,
      val callCount: AtomicLong = AtomicLong(0),
   ) : HealthCheck {
      override suspend fun check(): HealthCheckResult {
         callCount.incrementAndGet()
         while (blocker.get()) {
            Thread.sleep(25)
         }
         return HealthCheckResult.healthy("Slow")
      }
   }


   test("slow blocking check should not hold up existing checks") {

      val check1 = SlowHealthCheck(AtomicBoolean(false))
      val check2 = SlowHealthCheck(AtomicBoolean(true))
      val check3 = SlowHealthCheck(AtomicBoolean(false))

      val reg = HealthCheckRegistry {
         register("check1", check1, 1.milliseconds, 1.milliseconds)
         register("check2", check2, 1.milliseconds, 1.milliseconds)
         register("check3", check3, 1.milliseconds, 1.milliseconds)
      }

      delay(5)

      reg.status().apply {
         this.healthy shouldBe false
         this.healthchecks.size shouldBe 3

         this.healthchecks["check1"]?.result?.isHealthy shouldBe true
         this.healthchecks["check2"]?.result?.isHealthy shouldBe false
         this.healthchecks["check3"]?.result?.isHealthy shouldBe true
      }

      delay(5)

      withClue("Check 2 should still be waiting for first run to finish") {
         check2.callCount.get() shouldBe 1
      }

      check1.callCount.get() shouldBeGreaterThan 1
      check1.callCount.get() shouldBeGreaterThan 1

      //Unblock the slow 2nd check
      check2.blocker.set(false)

      eventually(1.seconds) {
         reg.status().apply {
            this.healthy shouldBe true
            this.healthchecks.size shouldBe 3

            this.healthchecks.forAll { it.value.result.isHealthy shouldBe true }
            check2.callCount.get() shouldBeGreaterThan 1
         }
      }
   }
})
