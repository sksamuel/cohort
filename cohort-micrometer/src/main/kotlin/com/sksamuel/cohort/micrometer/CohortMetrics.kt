package com.sksamuel.cohort.micrometer

import com.sksamuel.cohort.HealthCheckRegistry
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder

class CohortMetrics(private val healthCheckRegistry: HealthCheckRegistry) : MeterBinder {

  private val counters = mutableMapOf<Pair<String, Boolean>, Counter>()

  override fun bindTo(registry: MeterRegistry) {
    healthCheckRegistry.addSubscriber { name, check, result ->
      val counter = counters.getOrPut(Pair(name, result.isHealthy)) {
        Counter.builder("cohort.healthcheck")
          .tag("name", name)
          .tag("type", check::class.java.name)
          .tag("healthy", result.isHealthy.toString())
          .register(registry)
      }
      counter.increment()
    }
  }
}
