package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Abstract class [HealthCheck]s that are designed to warm up a service, and then complete.
 * Once the warmup phase has completed, they continue to return true for the duration of the service.
 */
@Deprecated("Replaced with specialized WarmupCheck's")
abstract class WarmupHealthCheck : HealthCheck {

   private val runner = WarmupRunner()
   private val started = AtomicBoolean(false)

   override suspend fun check(): HealthCheckResult {
      if (started.compareAndSet(false, true)) {
         runner.run(this)
      }
      return runner.result(this)
   }

   /**
    * How many iterations to run.
    */
   abstract val iterations: Int

   /**
    * Invoked before the first iteration.
    */
   open suspend fun start() {}

   /**
    * Invoked on each iteration with the current [iteration] number.
    */
   abstract suspend fun warm(iteration: Int)

   /**
    * Invoked after the last iteration.
    */
   open suspend fun close() {}
}

/**
 * Executes a [WarmupHealthCheck] until all iterations have completed.
 */
@Deprecated("Use the new Warmup mechanism")
internal class WarmupRunner {

   private val scope = CoroutineScope(Dispatchers.Default)
   private var completed = 0
   private var time = 0L
   private val error = AtomicReference<Throwable>()

   suspend fun run(warmup: WarmupHealthCheck) {
      scope.launch {

         val start = System.currentTimeMillis()
         warmup.start()

         repeat(warmup.iterations) { k ->
            runCatching {
               warmup.warm(k)
               completed++
            }.onFailure {
               error.set(it)
            }.onSuccess {
               error.set(null)
            }
         }

         warmup.close()

         time = System.currentTimeMillis() - start
      }
   }

   fun result(warmup: WarmupHealthCheck): HealthCheckResult {
      val t = error.get()
      return when {
         t != null -> HealthCheckResult.unhealthy("Warmup '${warmup.name}' error", t)
         completed == warmup.iterations -> HealthCheckResult.healthy("Warmup '${warmup.name}' has completed in ${time}ms")
         else -> HealthCheckResult.unhealthy("Warmup '${warmup.name}' has completed $completed iterations")
      }
   }
}

/**
 * Returns a new [WarmupHealthCheck] with the implementation delegated to the given [fn].
 */
@Deprecated("Use the new Warmup mechanism")
fun warmup(fn: suspend (Int) -> Unit, iter: Int) = object : WarmupHealthCheck() {
   override val iterations = iter
   override suspend fun warm(iteration: Int) {
      fn(iteration)
   }
}
