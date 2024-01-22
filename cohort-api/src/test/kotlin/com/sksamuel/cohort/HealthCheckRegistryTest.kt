package com.sksamuel.cohort

import com.sksamuel.cohort.threads.ThreadDeadlockHealthCheck
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec

class HealthCheckRegistryTest : FunSpec({

   test("duplicate name should throw error") {
      shouldThrowAny {
         HealthCheckRegistry {
            register(ThreadDeadlockHealthCheck())
            register(ThreadDeadlockHealthCheck())
         }
      }
   }

})
