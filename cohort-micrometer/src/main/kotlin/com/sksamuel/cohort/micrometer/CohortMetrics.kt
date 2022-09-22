package com.sksamuel.cohort.micrometer

import com.sksamuel.cohort.HealthCheckRegistry
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.MeterBinder

class CohortMetrics(private val healthCheckRegistry: HealthCheckRegistry) : MeterBinder {

   private val counters = mutableMapOf<Pair<String, Boolean>, Counter>()
   private val tags = mutableSetOf<Tag>()

   /**
    * Adds the given tags to the metrics.
    */
   fun tags(vararg tags: Tag) {
      this.tags.addAll(tags.toSet())
   }

   override fun bindTo(registry: MeterRegistry) {
      healthCheckRegistry.addSubscriber { name, check, result ->
         val counter = counters.getOrPut(Pair(name, result.isHealthy)) {
            Counter.builder("cohort.healthcheck")
               .tag("name", name)
               .tag("type", check::class.java.name)
               .tag("healthy", result.isHealthy.toString())
               .tags(tags)
               .register(registry)
         }
         counter.increment()
      }
   }
}
