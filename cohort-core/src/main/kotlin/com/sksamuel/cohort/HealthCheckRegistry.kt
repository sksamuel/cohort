@file:Suppress("unused")

package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A [HealthCheckRegistry] executes [HealthCheck]s based on provided schedules.
 *
 * All executions happen on the provided [CoroutineDispatcher].
 *
 * Individual checks are free to shift the dispatcher onto a [Dispatchers.IO] for IO calls.
 * It is recommended that the provided dispatcher has parallelism limited to 1.
 *
 * This registry creates one additional thread for its own use, to run the scheduler. This thread remains
 * in [Thread.State.BLOCKED] while waiting to fire a scheduled event, and this thread is not used
 * for the actual execution of the scheduled events.
 *
 * @param dispatcher this dispatcher will be used for executing the checks
 * @param startUnhealthy if true, then all checks will start in failed state until they pass.
 */
class HealthCheckRegistry(
   private val dispatcher: CoroutineDispatcher,
   private val startUnhealthy: Boolean = true,
   private val logUnhealthy: Boolean = true,
) {

   private val scheduler = Executors.newScheduledThreadPool(1)
   private val names = mutableSetOf<String>()
   private val checks = ConcurrentHashMap<String, HealthCheck>()
   private val warmups = ConcurrentHashMap<String, Warmup>()
   private val checkResults = ConcurrentHashMap<String, CheckStatus>()
   private val warmupResults = ConcurrentHashMap<String, WarmupStatus>()
   private val logger = KotlinLogging.logger {}
   private val subscribers = mutableListOf<Subscriber>()
   private val warmupScope = CoroutineScope(Dispatchers.Default)

   companion object {

      operator fun invoke(
         dispatcher: CoroutineDispatcher,
         configure: HealthCheckRegistry.() -> Unit
      ): HealthCheckRegistry {
         val registry = HealthCheckRegistry(dispatcher)
         registry.configure()
         return registry
      }
   }

   /**
    * Adds a new [Warmup] to this registry, which is started immediately.
    * Upon completion, the [Warmup.close] method is invoked and the healthcheck is removed.
    *
    * @param delay how long to wait before starting the warmups
    */
   fun warm(warmup: Warmup, delay: Duration = 1.seconds) = warm(warmup.name, warmup, delay)

   /**
    * Adds a new [Warmup] to this registry, which is started immediately.
    * Upon completion, the [Warmup.close] method is invoked and the healthcheck is removed.
    *
    * @param delay how long to wait before starting the warmups
    */
   fun warm(name: String, warmup: Warmup, delay: Duration = 1.seconds) {

      if (warmups.contains(name)) error("Warmup '$name' already registered")
      warmups.putIfAbsent(name, warmup)

      var completed = 0
      logger.warn { "Beginning warmup '$name' for ${warmup.iterations} iterations with ${warmup.interval} between iterations" }
      val start = System.currentTimeMillis()

      warmupScope.launch {
         delay(delay)
         warmup.start()
         repeat(warmup.iterations) {
            runCatching {
               warmup.warmup()
            }.onFailure { logger.warn(it) { "Warmup '$name' error" } }
            completed++
            warmupResults[name] = WarmupStatus(completed, warmup.iterations)
            delay(warmup.interval)
         }
      }.invokeOnCompletion {
         val time = System.currentTimeMillis() - start
         logger.warn { "Warmup '$name' has completed in ${time}ms" }
         warmup.close()
         warmups.remove(name)
      }
   }

   /**
    * Adds a new [HealthCheck] to this registry using the given duration for both initial delay and intervals.
    * The name is derived from the check class.
    */
   fun register(
      check: HealthCheck,
      delay: Duration,
   ): HealthCheckRegistry = register(check.name, check, delay, delay)

   /**
    * Adds a new [HealthCheck] to this registry using the given duration for both initial delay and intervals.
    */
   fun register(
      name: String,
      check: HealthCheck,
      delay: Duration,
   ): HealthCheckRegistry = register(name, check, delay, delay)

   /**
    * Adds a new [HealthCheck] to this registry with the given schedule.
    *
    * @param name the name is associated with the result in the output json.
    * This is useful to allow the same check to be registered multiple times against different configurations.
    */
   fun register(
      name: String,
      check: HealthCheck,
      initialDelay: Duration,
      checkInterval: Duration
   ): HealthCheckRegistry {
      require(check !is Warmup) { "Register Warmups using warm(check)" }

      if (checks.contains(name)) error("Check $name already registered")
      checks.putIfAbsent(name, check)

      if (startUnhealthy) {
         checkResults[name] =
            CheckStatus(0, 0, false, Instant.now(), HealthCheckResult.Unhealthy("Not yet executed", null))
      }

      scheduler.scheduleWithFixedDelay(
         {
            // we block the thread used by the scheduler, as we execute inside the provided coroutine dispatcher
            runBlocking {
               launch(dispatcher) {
                  run(name)
               }
            }
         },
         initialDelay.inWholeMilliseconds,
         checkInterval.inWholeMilliseconds,
         TimeUnit.MILLISECONDS,
      )

      return this
   }

   private suspend fun run(name: String) {
      val check = checks[name] ?: return
      try {
         val result = check.check()
         notifySubscribers(name, check, result)
         when (result) {
            is HealthCheckResult.Healthy -> success(name, result)
            is HealthCheckResult.Unhealthy -> failure(name, result)
         }
      } catch (t: Throwable) {
         val result = HealthCheckResult.Unhealthy("$name failed due to ${t.javaClass.name}", t)
         notifySubscribers(name, check, result)
         failure(name, result)
      }
   }

   private fun success(name: String, result: HealthCheckResult.Healthy) {

      val previous = checkResults[name]
      val successes = if (previous == null) 1 else previous.consecutiveSuccesses + 1

      checkResults[name] = CheckStatus(
         consecutiveSuccesses = successes,
         consecutiveFailures = 0, // reset to 0 when we have a success
         healthy = true,
         timestamp = Instant.now(),
         result = result
      )
   }

   private fun failure(name: String, result: HealthCheckResult.Unhealthy) {

      val previous = checkResults[name]
      val failures = if (previous == null) 1 else previous.consecutiveFailures + 1

      logger.warn { "HealthCheck $name reported $failures failures $result" }

      checkResults[name] = CheckStatus(
         consecutiveSuccesses = 0, // reset to 0 when we have a failure
         consecutiveFailures = failures,
         healthy = false,
         timestamp = Instant.now(),
         result = result
      )
   }

   fun status(): Health {
      val healthy = checkResults.values.all { it.healthy } && warmupResults.values.all { it.iterations == it.completed }
      return Health(healthy, warmupResults.toMap(), checkResults.toMap())
   }

   fun checks(): Set<HealthCheck> = checks.values.toSet()

   /**
    * Adds a [Subscriber] to this registry, which will be invoked each time a health check completes.
    *
    * Note: This method is not thread safe.
    */
   fun addSubscriber(subscriber: Subscriber) {
      subscribers.add(subscriber)
   }

   private suspend fun notifySubscribers(name: String, check: HealthCheck, result: HealthCheckResult) {
      subscribers.forEach {
         runCatching {
            it.invoke(name, check, result)
         }.onFailure { logger.warn(it) { "Error notifying subscriber of health check $name" } }
      }
   }
}

fun interface Subscriber {
   suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult)
}

data class CheckStatus(
   val consecutiveSuccesses: Int,
   val consecutiveFailures: Int,
   val healthy: Boolean, // overall health status, true if all checks are healthy
   val timestamp: Instant,
   val result: HealthCheckResult,
)

data class WarmupStatus(val completed: Int, val iterations: Int)

data class Health(
   val healthy: Boolean,
   val warmups: Map<String, WarmupStatus>,
   val results: Map<String, CheckStatus>
)

fun <A> Health.fold(
   ifUnhealthy: (Map<String, CheckStatus>) -> A,
   ifHealthy: (Map<String, CheckStatus>) -> A
): A {
   return when (healthy) {
      true -> ifHealthy(results)
      false -> ifUnhealthy(results)
   }
}
