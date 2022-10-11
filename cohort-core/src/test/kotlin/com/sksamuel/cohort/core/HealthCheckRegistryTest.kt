package com.sksamuel.cohort.core

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.cpu.FibWarmup
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec

class HealthCheckRegistryTest : FunSpec({

   test("duplicate warmup name should throw error") {
      shouldThrowAny {
         HealthCheckRegistry() {
            warm(FibWarmup(), 1000)
            warm(FibWarmup(), 1000)
         }
      }
   }

})
