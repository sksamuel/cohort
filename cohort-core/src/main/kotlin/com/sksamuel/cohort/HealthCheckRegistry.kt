@file:Suppress("unused")

package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

   private val scheduler = Executors.newScheduledThreadPool(1, NamedThreadFactory("cohort-scheduler"))
   private val names = mutableSetOf<String>()

   // contains all the registered healthchecks
   private val checks = ConcurrentHashMap<String, HealthCheck>()

   // contains all the registered warmups
   private val warmups = ConcurrentHashMap<String, WarmupHealthCheck>()

   private val results = ConcurrentHashMap<String, HealthCheckStatus>()

   private val logger = KotlinLogging.logger {}
   private val subscribers = mutableListOf<Subscriber>()
   private val warmupScope = CoroutineScope(Dispatchers.Default)

   companion object {
      val DEFAULT_INTERVAL = 5.seconds

      operator fun invoke(
         configure: HealthCheckRegistry.() -> Unit
      ): HealthCheckRegistry = invoke(Dispatchers.Default, configure)

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
    * Adds a new [HealthCheck] to this registry using the [DEFAULT_INTERVAL] for both initial delay and intervals.
    * The name is derived from the check class.
    */
   fun register(
      check: HealthCheck,
   ): HealthCheckRegistry = register(check.name, check, DEFAULT_INTERVAL, DEFAULT_INTERVAL)

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
    *             This is useful to allow the same check to be registered multiple times against different configurations.
    *             No healthcheck can be registered twice with the same name.
    */
   fun register(
      name: String,
      check: HealthCheck,
      initialDelay: Duration,
      checkInterval: Duration
   ): HealthCheckRegistry {

      if (checks.containsKey(name)) error("Check $name already registered")
      checks.putIfAbsent(name, check)

      if (startUnhealthy) {
         results[name] =
            HealthCheckStatus(0, 0, false, Instant.now(), HealthCheckResult.unhealthy("Not yet executed", null))
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
         when (result.status) {
            HealthStatus.Healthy -> success(name, result)
            HealthStatus.Unhealthy -> failure(name, result)
            HealthStatus.Startup -> failure(name, result)
         }
      } catch (t: Throwable) {
         val result = HealthCheckResult.unhealthy("$name failed due to ${t.javaClass.name}", t)
         notifySubscribers(name, check, result)
         failure(name, result)
      }
   }

   private fun success(name: String, result: HealthCheckResult) {

      val previous = results[name]
      val successes = if (previous == null) 1 else previous.consecutiveSuccesses + 1

      results[name] = HealthCheckStatus(
         consecutiveSuccesses = successes,
         consecutiveFailures = 0, // reset to 0 when we have a success
         healthy = true,
         timestamp = Instant.now(),
         result = result
      )
   }

   private fun failure(name: String, result: HealthCheckResult) {

      val previous = results[name]
      val failures = if (previous == null) 1 else previous.consecutiveFailures + 1

      logger.warn { "HealthCheck $name reported $failures failures $result" }

      results[name] = HealthCheckStatus(
         consecutiveSuccesses = 0, // reset to 0 when we have a failure
         consecutiveFailures = failures,
         healthy = false,
         timestamp = Instant.now(),
         result = result
      )
   }

   /**
    * Returns the [Health] of the system.
    *
    * A system is considered healthy if all the healthchecks are in healthy state.
    */
   fun status(): Health {
      val healthy = results.values.all { it.healthy }
      return Health(healthy, results.toMap())
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

/**
 * Tracks the health for a [HealthCheck].
 */
data class HealthCheckStatus(
   val consecutiveSuccesses: Int,
   val consecutiveFailures: Int,
   val healthy: Boolean, // overall health status, true if all checks are healthy
   val timestamp: Instant,
   val result: HealthCheckResult,
)

data class WarmupStatus(val completed: Int, val iterations: Int)

/**
 * A system is considered healthy if all the healthchecks are in healthy state, and each warmup
 * has completed all it's iterations.
 */
data class Health(
   val healthy: Boolean,
   val healthchecks: Map<String, HealthCheckStatus>
)

fun <A> Health.fold(
   ifUnhealthy: (Map<String, HealthCheckStatus>) -> A,
   ifHealthy: (Map<String, HealthCheckStatus>) -> A
): A {
   return when (healthy) {
      true -> ifHealthy(healthchecks)
      false -> ifUnhealthy(healthchecks)
   }
}
