package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.Check
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.util.AttributeKey

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
      val routing: Routing.() -> Unit = {
        config.endpoints.forEach { (endpoint, checks) ->
          get(endpoint) {
            val (status, responseBody) = Pair(HttpStatusCode.OK, "")
            call.respondText(responseBody, ContentType.Application.Json, status)
            finish()
          }
        }
      }
      pipeline.featureOrNull(Routing)?.apply(routing) ?: pipeline.install(Routing, routing)
      proceed()
    }
  }
}

class CohortConfiguration {
  val endpoints = mutableMapOf<String, List<Check>>()
  fun configure(endpoint: String, vararg checks: Check) {
    endpoints[endpoint] = checks.toList()
  }
}
