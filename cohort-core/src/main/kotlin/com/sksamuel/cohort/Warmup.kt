package com.sksamuel.cohort

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Interface for startup procedures that are designed to warm up the JVM.
 * A warmup contains state, so an instance should not be reused.
 */
abstract class Warmup : AutoCloseable {

   abstract val iterations: Int

   // how many to wait between each iteration
   abstract val interval: Duration

   open val name: String = this::class.java.name.removePrefix("com.sksamuel.")

   /**
    * Invoked before the first iteration.
    */
   open suspend fun start() {}

   /**
    * Invoked on each iteration.
    */
   abstract suspend fun warmup()

   /**
    * Invoked after the last iteration.
    */
   override fun close() {}
}

class FunctionWarmup(
   override val iterations: Int = 10000,
   override val interval: Duration = 1.milliseconds,
   private val fn: () -> Unit
) : Warmup() {
   override suspend fun warmup() {
      fn()
   }
}
