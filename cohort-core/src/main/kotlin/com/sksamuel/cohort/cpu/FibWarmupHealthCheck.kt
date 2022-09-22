package com.sksamuel.cohort.cpu

import com.sksamuel.cohort.WarmupHealthCheck
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class FibWarmupHealthCheck(
   override val iterations: Int = 10000,
   override val interval: Duration = 1.milliseconds,
) : WarmupHealthCheck() {

   override val name: String = "fib_warmup"

   private fun fib(n: Int): Int = when (n) {
      0, 1 -> 1
      else -> fib(n - 1) + fib(n - 2)
   }

   override suspend fun warmup() {
      fib(Random.nextInt(0, 10))
   }
}
