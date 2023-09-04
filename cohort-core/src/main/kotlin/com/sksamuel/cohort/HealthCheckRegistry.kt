@file:Suppress("unused")

package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
 */
class HealthCheckRegistry(
   private val dispatcher: CoroutineDispatcher,
) : AutoCloseable {

   private val scheduler = Executors.newScheduledThreadPool(1, NamedThreadFactory("cohort-healthcheck-scheduler"))
   private val names = mutableSetOf<String>()

   // tracks which checks are startup checks
   @Deprecated("Replaced with WarmupRegistry")
   private val startups = ConcurrentHashMap<String, Boolean>()
   private val checks = ConcurrentHashMap<String, HealthCheck>()
   private val statuses = ConcurrentHashMap<String, HealthCheckStatus>()

   private val logger = LoggerFactory.getLogger(WarmupRegistry::class.java)
   private val subscribers = ConcurrentHashMap.newKeySet<Subscriber>()

   @Deprecated("Replaced with WarmupRegistry")
   private val warmupScope = CoroutineScope(Dispatchers.Default)

   internal var warmupRegistry: WarmupRegistry? = null
   var startUnhealthy: Boolean = true
   var logUnhealthy: Boolean = true

   init {
      Runtime.getRuntime().addShutdownHook(Thread {
         logger.info("Cohort HealthCheckRegistry shuthook hook is executing")
         close()
      })
   }

   companion object {
      val DEFAULT_INTERVAL = 5.seconds

      operator fun invoke(
         configure: HealthCheckRegistry.() -> Unit
      ): HealthCheckRegistry = invoke(Dispatchers.Default, configure)

      @Deprecated("Use HealthCheckRegistry() or HealthCheckRegistry(dispatcher) and invoke startUnhealthy/logUnhealthy inside the configuration block")
      operator fun invoke(
         dispatcher: CoroutineDispatcher,
         startUnhealthy: Boolean = true,
         logUnhealthy: Boolean = true,
         configure: HealthCheckRegistry.() -> Unit
      ): HealthCheckRegistry {
         val registry = HealthCheckRegistry(dispatcher)
         registry.startUnhealthy = startUnhealthy
         registry.logUnhealthy = logUnhealthy
         registry.configure()
         return registry
      }

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
    * Adds a [HealthCheck] to this registry using the [DEFAULT_INTERVAL] for both initial delay and intervals.
    * The name used for this check is the default name supplied by the healthcheck instance.
    */
   fun register(
      check: HealthCheck,
   ): HealthCheckRegistry = register(check.name, check)

   /**
    * Adds a [HealthCheck] to this registry, with the specified [name], using the [DEFAULT_INTERVAL]
    * for both initial delay and intervals.
    *
    * @param name the name is associated with the [check] in the output json. By supplying a custom
    *             name, the same check can be registered multiple times. o healthcheck can be registered
    *             more than once with a repeated name.
    */
   fun register(
      name: String,
      check: HealthCheck,
   ): HealthCheckRegistry = register(name, check, DEFAULT_INTERVAL, DEFAULT_INTERVAL)

   /**
    * Adds a [HealthCheck] to this registry using the given [delay] for both initial delay and intervals.
    * The name used for this check is the default name supplied by the healthcheck instance.
    */
   fun register(
      check: HealthCheck,
      delay: Duration,
   ): HealthCheckRegistry = register(check.name, check, delay, delay)

   /**
    * Adds a new [HealthCheck] to this registry, with the specified [name], using the given [delay]
    * for both initial delay and intervals.
    *
    * @param name the name is associated with the [check] in the output json. By supplying a custom
    *             name, the same check can be registered multiple times. o healthcheck can be registered
    *             more than once with a repeated name.
    */
   fun register(
      name: String,
      check: HealthCheck,
      delay: Duration,
   ): HealthCheckRegistry = register(name, check, delay, delay)

   /**
    * Adds a new [HealthCheck] to this registry with the given schedule.
    *
    * @param name the name is associated with the [check] in the output json. By supplying a custom
    *             name, the same check can be registered multiple times. o healthcheck can be registered
    *             more than once with a repeated name.
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
         statuses[name] =
            HealthCheckStatus(0, 0, Instant.now(), HealthCheckResult.unhealthy("Not yet executed", null))
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
         }
      } catch (t: Throwable) {
         val result = HealthCheckResult.unhealthy("$name failed due to ${t.javaClass.name}", t)
         notifySubscribers(name, check, result)
         failure(name, result)
      }
   }

   private fun success(name: String, result: HealthCheckResult) {

      val previous = statuses[name]
      val successes = if (previous == null) 1 else previous.consecutiveSuccesses + 1

      statuses[name] = HealthCheckStatus(
         consecutiveSuccesses = successes,
         consecutiveFailures = 0, // reset to 0 when we have a success
         timestamp = Instant.now(),
         result = result
      )
   }

   private fun failure(name: String, result: HealthCheckResult) {

      val previous = statuses[name]
      val failures = if (previous == null) 1 else previous.consecutiveFailures + 1

      logger.warn("HealthCheck $name reported $failures failures $result")

      statuses[name] = HealthCheckStatus(
         consecutiveSuccesses = 0, // reset to 0 when we have a failure
         consecutiveFailures = failures,
         timestamp = Instant.now(),
         result = result
      )
   }

   /**
    * Returns the [ServiceHealth] of the system.
    *
    * A service is considered healthy if all the healthchecks are in healthy state.
    */
   fun status(): ServiceHealth {
      val warmupState = warmupRegistry?.state() ?: WarmupState.Completed
      val healthy = statuses.values.all { it.result.isHealthy } && warmupState == WarmupState.Completed
      return ServiceHealth(healthy, warmupState, statuses.toMap())
   }

   /**
    * Adds a [Subscriber] to this registry, which will be invoked each time a health check is invoked.
    */
   fun addSubscriber(subscriber: Subscriber) {
      subscribers.add(subscriber)
   }

   private suspend fun notifySubscribers(name: String, check: HealthCheck, result: HealthCheckResult) {
      subscribers.forEach {
         runCatching {
            it.invoke(name, check, result)
         }.onFailure { logger.warn("Error notifying subscriber of health check $name", it) }
      }
   }

   override fun close() {
      scheduler.shutdown()
   }
}

fun interface Subscriber {
   suspend fun invoke(name: String, check: HealthCheck, result: HealthCheckResult)
}

/**
 * Tracks the health for an individual [HealthCheck].
 */
data class HealthCheckStatus(
   val consecutiveSuccesses: Int,
   val consecutiveFailures: Int,
   val timestamp: Instant, // time the health check was last queried
   val result: HealthCheckResult, // result of the last invocation
)

/**
 * A service is considered healthy if all the healthchecks are in healthy state and warmups are not running.
 */
data class ServiceHealth(
   val healthy: Boolean,
   val warmups: WarmupState,
   val healthchecks: Map<String, HealthCheckStatus>
)

fun <A> ServiceHealth.fold(
   ifUnhealthy: (Map<String, HealthCheckStatus>) -> A,
   ifHealthy: (Map<String, HealthCheckStatus>) -> A
): A {
   return when (healthy) {
      true -> ifHealthy(healthchecks)
      false -> ifUnhealthy(healthchecks)
   }
}
