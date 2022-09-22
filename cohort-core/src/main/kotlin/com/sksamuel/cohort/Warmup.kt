package com.sksamuel.cohort

import kotlin.time.Duration

/**
 * Interface for startup procedures that are designed to warm up the JVM.
 * A warmup contains state, so an instance should not be reused.
 */
abstract class Warmup {

   abstract val iterations: Int

   // how many to wait between each iteration
   abstract val interval: Duration

   open val name: String = this::class.java.name.removePrefix("com.sksamuel.")

   /**
    * Invoked before the first iteration.
    */
   open fun start() {}

   /**
    * Invoked on each iteration.
    */
   abstract suspend fun warmup()

   /**
    * Invoked after the last iteration.
    */
   open fun close() {}
}
