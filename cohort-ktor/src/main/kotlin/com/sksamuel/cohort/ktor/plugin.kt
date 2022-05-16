package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.endpoints.CohortConfiguration
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.application
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.routing.Routing
import io.ktor.util.AttributeKey

val CohortConfigAttributeKey: AttributeKey<CohortConfiguration> = AttributeKey("CohortConfigAttributeKey")

class Cohort private constructor(
  private val config: CohortConfiguration
) {

  companion object Feature : ApplicationFeature<Application, CohortConfiguration, Cohort> {
    override val key = AttributeKey<Cohort>("Cohort")
    override fun install(pipeline: Application, configure: CohortConfiguration.() -> Unit) =
      Cohort(CohortConfiguration().apply(configure)).apply { interceptor(pipeline) }
  }

  fun interceptor(pipeline: Application) {
    pipeline.intercept(ApplicationCallPipeline.Monitoring) {
      this.application.attributes.put(CohortConfigAttributeKey, config)
      val routing: Routing.() -> Unit = { cohort() }
      pipeline.featureOrNull(Routing)?.apply(routing) ?: pipeline.install(Routing, routing)
      proceed()
    }
  }
}
