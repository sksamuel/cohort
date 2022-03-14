package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.heap.Heapdump
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
import java.time.ZoneOffset

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

        if (config.heapdump) {
          get("cohort/heapdump") {
            val live = call.request.queryParameters["live"].toBoolean()
            val dump = Heapdump.run(live)
            call.respondText(dump, ContentType.Text.Plain, HttpStatusCode.OK)
          }
        }

        config.healthchecks.forEach { (endpoint, registry) ->
          get(endpoint) {

            val status = registry.status()

            val results = status.results.map {
              ResultJson(
                name = it.key,
                healthy = it.value.healthy,
                lastCheck = it.value.timestamp.atOffset(ZoneOffset.UTC).toString(),
                message = it.value.result.message,
                cause = it.value.result.cause?.stackTraceToString(),
                consecutiveSuccesses = it.value.consecutiveSuccesses,
                consecutiveFailures = it.value.consecutiveFailures,
              )
            }
            val json = mapper.writeValueAsString(results)

            val httpStatusCode = when (status.healthy) {
              true -> HttpStatusCode.OK
              false -> HttpStatusCode.ServiceUnavailable
            }

            call.respondText(json, ContentType.Application.Json, httpStatusCode)
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

  val healthchecks = mutableMapOf<String, HealthCheckRegistry>()
  var heapdump: Boolean = false

  fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
    healthchecks[endpoint] = registry
  }

}
