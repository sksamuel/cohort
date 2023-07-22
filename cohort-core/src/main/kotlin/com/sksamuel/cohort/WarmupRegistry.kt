package com.sksamuel.cohort

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class WarmupRegistry(
   dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

   private val logger = LoggerFactory.getLogger(WarmupRegistry::class.java)
   private val scope = CoroutineScope(dispatcher)
   private val warmups = ConcurrentHashMap<String, WarmupState>()

   companion object {
      operator fun invoke(
         dispatcher: CoroutineDispatcher = Dispatchers.Default,
         configure: WarmupRegistry.() -> Unit
      ): WarmupRegistry {
         val registry = WarmupRegistry(dispatcher)
         registry.configure()
         return registry
      }
   }

   /**
    * Adds a [Warmup] to this registry using the default name.
    * This warmup is invoked until [duration] has expired, at which point the check is completed.
    *
    * Warmups are intended to be used to warm up the JVM for better performance once the system is ready
    * to start accepting requests.
    */
   fun register(warmup: Warmup, duration: Duration) {

      val existing = warmups.put(warmup.name, WarmupState.Running)
      if (existing != null)
         error("Warmup registry already contains a warmup with the name ${warmup.name}")

      scope.launch {
         logger.info("Starting warmup ${warmup.name} for $duration")
         var iterations = 0
         val end = System.currentTimeMillis() + duration.inWholeMilliseconds
         while (System.currentTimeMillis() < end) {
            try {
               warmup.warm(iterations++)
            } catch (e:Exception) {
               logger.warn("Error running Warmup", e)
            }
         }
         warmup.close()
         logger.info("Warmup ${warmup.name} has completed")
         warmups[warmup.name] = WarmupState.Completed
      }
   }

   fun state(): WarmupState {
      return if (warmups.all { it.value == WarmupState.Completed }) WarmupState.Completed else WarmupState.Running
   }
}

enum class WarmupState {
   Running, Completed
}
