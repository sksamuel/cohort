package com.sksamuel.cohort.micrometer

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.HealthStatus
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.MeterBinder

class CohortMetrics(private val healthCheckRegistry: HealthCheckRegistry) : MeterBinder {

   private val counters = mutableMapOf<Pair<String, HealthStatus>, Counter>()
   private val tags = mutableSetOf<Tag>()

   /**
    * Adds the given tags to the metrics.
    */
   fun tags(vararg tags: Tag) {
      this.tags.addAll(tags.toSet())
   }

   override fun bindTo(registry: MeterRegistry) {
      healthCheckRegistry.addSubscriber { name, check, result ->
         val counter = counters.getOrPut(Pair(name, result.status)) {
            Counter.builder("cohort.healthcheck")
               .tag("name", name)
               .tag("type", check::class.java.name)
               .tag("status", result.status.name)
               .tags(tags)
               .register(registry)
         }
         counter.increment()
      }
   }
}
