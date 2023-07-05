package com.sksamuel.cohort

interface Warmup {

   // the default name used in logs and metrics unless overridden when registering or subclassing.
   val name: String
      get() {
         val jvmName = this::class.java.name
         return if (jvmName.startsWith("com.sksamuel.cohort"))
            jvmName.removePrefix("com.sksamuel.")
         else jvmName
      }

   /**
    * Invoked before the first iteration.
    */
   suspend fun start() {}

   /**
    * Execute an iteration of this warmup.
    *
    * @param iteration the iteration count of this invocation
    */
   suspend fun warm(iteration: Int)

   /**
    * Invoked once the warmup has completed.
    */
   suspend fun close() {}
}
