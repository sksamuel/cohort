package com.sksamuel.cohort

/**
 * Interface for startup procedures that are designed to warm up a service.
 */
interface Warmup {

   // the default name used in logs and metrics unless overridden when registering.
   val name: String
      get() = this::class.java.name.removePrefix("com.sksamuel.")

   /**
    * Invoked before the first iteration.
    */
   suspend fun start() {}

   /**
    * Invoked on each iteration with the current [iteration] number.
    */
   suspend fun warm(iteration: Int)

   /**
    * Invoked after the last iteration.
    */
   suspend fun close() {}
}

/**
 * Returns a new [Warmup] with the implementation delegated to the given [fn].
 */
fun warmup(fn: suspend (Int) -> Unit) = object : Warmup {
   override suspend fun warm(iteration: Int) {
      fn(iteration)
   }
}
