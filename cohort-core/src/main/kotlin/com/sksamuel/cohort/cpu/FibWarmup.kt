package com.sksamuel.cohort.cpu

import com.sksamuel.cohort.WarmupHealthCheck
import kotlin.random.Random

class FibWarmup(
   private val depth: Int = 32,
   override val iterations: Int = 1000,
) : WarmupHealthCheck() {

   override val name: String = "fib_warmup"

   private fun fib(n: Int): Int = when (n) {
      0, 1 -> 1
      else -> fib(n - 1) + fib(n - 2)
   }

   override suspend fun warm(iteration: Int) {
      fib(Random.nextInt(0, depth))
   }
}

suspend fun main() {
   val w = FibWarmup()
   repeat(1000) {
      w.warm(it)
      println(it)
   }
}
