package com.sksamuel.cohort

import com.sksamuel.cohort.HealthCheckRegistry.Companion.DEFAULT_INTERVAL
import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.testcontainers.shaded.org.bouncycastle.oer.its.ieee1609dot2.basetypes.Duration
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

class HealthCheckRegistryTest : FunSpec({

   test("duplicate name should throw error") {
      shouldThrowAny {
         HealthCheckRegistry {
            register(ThreadDeadlockHealthCheck())
            register(ThreadDeadlockHealthCheck())
         }
      }
   }

   class SlowHealthCheck(val id: String, val blocker: AtomicBoolean = AtomicBoolean(true),
                         val callCount: AtomicLong = AtomicLong(0),

   ) : HealthCheck {
      override suspend fun check(): HealthCheckResult {
         callCount.incrementAndGet()
         while(blocker.get()) {
            Thread.sleep(1)
         }
         return HealthCheckResult.healthy("Slow")
      }
   }


   test("slow blocking check should not hold up existing checks") {
       val check1 = SlowHealthCheck("1").apply { blocker.set(false) }
       val check2 = SlowHealthCheck("2")
       val check3 = SlowHealthCheck("3").apply { blocker.set(false) }

      val reg = HealthCheckRegistry {
         register("check1", check1, 1.milliseconds, 1.milliseconds)
         register("check2", check2, 1.milliseconds,  1.milliseconds)
         register("check3", check3,  1.milliseconds,  1.milliseconds)
      }

      delay(5)

      reg.status().apply {
         this.healthy shouldBe false
         this.healthchecks.size shouldBe 3

         this.healthchecks["check2"]?.result?.isHealthy shouldBe false
         withClue("Check 2 should still be waiting for first run to finish") { check2.callCount.get() shouldBe 1 }
         this.healthchecks["check3"]?.result?.isHealthy shouldBe true
      }

      //Unblock the slow 2nd check
      check2.blocker.set(false)

      delay(3)

      reg.status().apply {
         this.healthy shouldBe true
         this.healthchecks.size shouldBe 3

         this.healthchecks.all { it.value.result.isHealthy shouldBe true }
         check2.callCount.get() shouldBeGreaterThan 1
      }

   }

})
