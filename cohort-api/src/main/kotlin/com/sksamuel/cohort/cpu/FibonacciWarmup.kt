//package com.sksamuel.cohort.cpu
//
//import com.sksamuel.cohort.Warmup
//import kotlin.random.Random
//
//class FibonacciWarmup(
//   private val depth: Int = 32,
//) : Warmup {
//
//   override val name: String = "fibonacci_warmup"
//
//   private fun fib(n: Int): Int = when (n) {
//      0, 1 -> 1
//      else -> fib(n - 1) + fib(n - 2)
//   }
//
//   override suspend fun warm(iteration: Int) {
//      fib(Random.nextInt(0, depth))
//   }
//}
