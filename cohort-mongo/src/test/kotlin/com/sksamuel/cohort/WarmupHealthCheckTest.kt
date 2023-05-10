package com.sksamuel.cohort

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.delay

class WarmupHealthCheckTest : FunSpec({

   test("warmups should only warmup once") {
      val results = mutableListOf<Int>()
      val warmup = object : WarmupHealthCheck() {
         override val iterations: Int = 10
         override suspend fun warm(iteration: Int) {
            results.add(iteration)
         }
      }
      warmup.check()
      warmup.check()
      warmup.check()
      delay(100)
      results.shouldContainExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
   }
})
