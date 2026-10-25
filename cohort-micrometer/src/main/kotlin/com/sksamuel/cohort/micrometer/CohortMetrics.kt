package com.sksamuel.cohort.micrometer

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.HealthStatus
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.MeterBinder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

class CohortMetrics(private val healthCheckRegistry: HealthCheckRegistry) : MeterBinder {

   // Key includes the MeterRegistry so a single CohortMetrics bound to two different
   // registries doesn't reuse the first registry's Counter for the second. Without the
   // registry in the key, computeIfAbsent saw an existing counter (registered against the
   // first registry) and skipped registration in the second.
   private val counters = ConcurrentHashMap<Triple<MeterRegistry, String, HealthStatus>, Counter>()
   private val tags = CopyOnWriteArraySet<Tag>()

   /**
    * Adds the given tags to the metrics.
    */
   fun tags(vararg tags: Tag) {
      this.tags.addAll(tags.toSet())
   }

   override fun bindTo(registry: MeterRegistry) {
      // Snapshot tags at bind-time so a later tags(...) call from another thread can't race
      // with the subscriber iterating the set.
      val snapshot = tags.toList()
      healthCheckRegistry.addSubscriber { name, check, result ->
         val counter = counters.computeIfAbsent(Triple(registry, name, result.status)) {
            Counter.builder("cohort.healthcheck")
               .tag("name", name)
               .tag("type", check::class.java.name)
               .tag("status", result.status.name)
               .tags(snapshot)
               .register(registry)
         }
         counter.increment()
      }
   }
}
