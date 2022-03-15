package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.heap.Heapdump
import com.sksamuel.cohort.jvm.getJvmDetails
import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
import com.sksamuel.cohort.os.getOperatingSystem
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.featureOrNull
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.util.AttributeKey
import io.ktor.util.getOrFail
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

        config.logManager?.let { manager ->

          data class LogInfo(val levels: List<String>, val loggers: List<Logger>)

          get("cohort/logging") {
            runCatching {
              val levels = manager.levels()
              val loggers = manager.loggers()
              mapper.writeValueAsString(LogInfo(levels, loggers))
            }.fold(
              { call.respondText(it, ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respond(HttpStatusCode.InternalServerError, it) },
            )

          }

          put("cohort/logging/{name}/{level}") {
            val name = call.parameters.getOrFail("name")
            val level = call.parameters.getOrFail("level")
            manager.set(name, level).fold(
              { call.respond(HttpStatusCode.OK) },
              { call.respond(HttpStatusCode.InternalServerError, it) },
            )
          }
        }

        if (config.jvmInfo) {
          get("cohort/jvm") {
            getJvmDetails().fold(
              {
                val json = mapper.writeValueAsString(it)
                call.respondText(json, ContentType.Application.Json, HttpStatusCode.OK)
              },
              { call.respond(HttpStatusCode.InternalServerError, it) },
            )
          }
        }

        if (config.operatingSystem) {
          get("cohort/os") {
            call.respondText(
              mapper.writeValueAsString(getOperatingSystem()),
              ContentType.Application.Json,
              HttpStatusCode.OK
            )
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
  var operatingSystem: Boolean = false
  var logManager: LogManager? = null

  // set to true to enable the /cohort/jvm endpoint which returns JVM information
  var jvmInfo: Boolean = false

  fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
    healthchecks[endpoint] = registry
  }

}
