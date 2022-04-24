package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.endpoints.CohortConfiguration
import com.sksamuel.cohort.endpoints.LogInfo
import com.sksamuel.cohort.endpoints.ResultJson
import com.sksamuel.cohort.endpoints.toJson
import com.sksamuel.cohort.gc.getGcInfo
import com.sksamuel.cohort.heap.getHeapDump
import com.sksamuel.cohort.jvm.getJvmDetails
import com.sksamuel.cohort.memory.getMemoryInfo
import com.sksamuel.cohort.os.getOperatingSystem
import com.sksamuel.cohort.system.getSysProps
import com.sksamuel.cohort.threads.getThreadDump
import com.sksamuel.tabby.results.sequence
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

        if (config.heapDump) {
          get("cohort/heapdump") {
            getHeapDump().fold(
              { call.respond(HttpStatusCode.OK, it) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.memory) {
          get("cohort/memory") {
            getMemoryInfo().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        config.dataSources.let { dsm ->
          if (dsm.isNotEmpty()) {
            get("cohort/datasources") {
              dsm.map { it.info() }.sequence().fold(
                { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
                {
                  call.respondText(
                    it.stackTraceToString(),
                    ContentType.Text.Plain,
                    HttpStatusCode.InternalServerError
                  )
                }
              )
            }
          }
        }

        config.migrations?.let { m ->
          get("cohort/dbmigration") {
            m.migrations().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        config.logManager?.let { manager ->
          get("cohort/logging") {
            runCatching {
              val levels = manager.levels()
              val loggers = manager.loggers()
              LogInfo(levels, loggers)
            }.fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }

          put("cohort/logging/{name}/{level}") {
            val name = call.parameters.getOrFail("name")
            val level = call.parameters.getOrFail("level")
            manager.set(name, level).fold(
              { call.respond(HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.jvmInfo) {
          get("cohort/jvm") {
            getJvmDetails().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.gc) {
          get("cohort/gc") {
            getGcInfo().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.threadDump) {
          get("cohort/threaddump") {
            getThreadDump().fold(
              { call.respondText(it, ContentType.Text.Plain, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.sysprops) {
          get("cohort/sysprops") {
            getSysProps().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.operatingSystem) {
          get("cohort/os") {
            getOperatingSystem().fold(
              { call.respondText(it.toJson(), ContentType.Application.Json, HttpStatusCode.OK) },
              { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
            )
          }
        }

        if (config.hooks.isNotEmpty()) {
          get("cohort/shutdown") {
            config.hooks.forEach { it.run() }
            call.respond(HttpStatusCode.OK)
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

            val httpStatusCode = when (status.healthy) {
              true -> HttpStatusCode.OK
              false -> HttpStatusCode.ServiceUnavailable
            }

            call.respondText(results.toJson(), ContentType.Application.Json, httpStatusCode)
            finish()
          }
        }
      }
      pipeline.featureOrNull(Routing)?.apply(routing) ?: pipeline.install(Routing, routing)
      proceed()
    }
  }
}
