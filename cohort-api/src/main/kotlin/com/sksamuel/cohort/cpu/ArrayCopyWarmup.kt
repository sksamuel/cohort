//package com.sksamuel.cohort.cpu
//
//import com.sksamuel.cohort.Warmup
//import kotlin.random.Random
//
//class ArrayCopyWarmup : Warmup {
//   private var counter = 0
//   override suspend fun warm(iteration: Int) {
//      val arraySize = Random.nextInt(1000, 100_000)
//      val result = Array(arraySize) { it }.copyOf()
//      result[Random.nextInt(0, arraySize - 1)] = result.random()
//      counter += result.random()
//      if (Random.nextInt(1, 2) == 0)
//         println(counter.toString())
//   }
//}
