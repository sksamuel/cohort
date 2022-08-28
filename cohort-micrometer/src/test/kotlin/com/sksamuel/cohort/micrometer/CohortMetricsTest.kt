package com.sksamuel.cohort.micrometer

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.HealthCheckResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

object FooHealthCheck : HealthCheck {
  override suspend fun check(): HealthCheckResult = HealthCheckResult.Healthy("foo")
}

object BarHealthCheck : HealthCheck {
  override suspend fun check(): HealthCheckResult = HealthCheckResult.Unhealthy("bar")
}

class CohortMetricsTest : FunSpec() {

  init {
    test("should collect metrics") {
      val registry = HealthCheckRegistry(Dispatchers.Default) {
        this.register("foo", FooHealthCheck, 5.seconds)
        this.register("bar", BarHealthCheck, 3.seconds)
      }
      val mm = SimpleMeterRegistry()
      CohortMetrics(registry).bindTo(mm)
      mm.metersAsString shouldBe ""
      delay(4.seconds)
      mm.metersAsString shouldBe "cohort.healthcheck(COUNTER)[healthy='false', name='bar', type='com.sksamuel.cohort.micrometer.BarHealthCheck']; count=1.0"
      delay(3.seconds)
      mm.metersAsString shouldBe "cohort.healthcheck(COUNTER)[healthy='false', name='bar', type='com.sksamuel.cohort.micrometer.BarHealthCheck']; count=2.0\ncohort.healthcheck(COUNTER)[healthy='true', name='foo', type='com.sksamuel.cohort.micrometer.FooHealthCheck']; count=1.0"
    }
  }
}
