package com.sksamuel.cohort.core

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.cpu.FibWarmup
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.time.Duration.Companion.seconds

class WarmupTest : FunSpec() {
   init {
      test("warmup happy path") {
         val registry = HealthCheckRegistry {
            register(FibWarmup(), 1.seconds)
         }
         eventually(5.seconds) {
            registry.status().healthy shouldBe true
         }
         registry.status().healthchecks["fib_warmup"]!!.result.message shouldStartWith "Warmup 'fib_warmup' has completed in"
      }
   }
}
