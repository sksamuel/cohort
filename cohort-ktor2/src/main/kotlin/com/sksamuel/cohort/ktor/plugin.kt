package com.sksamuel.cohort.ktor

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.db.DataSourceManager
import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.gc.gcinfo
import com.sksamuel.cohort.heap.getHeapDump
import com.sksamuel.cohort.jvm.getJvmDetails
import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
import com.sksamuel.cohort.os.getOperatingSystem
import com.sksamuel.cohort.system.getSysProps
import com.sksamuel.cohort.threads.getThreadDump
import com.sksamuel.tabby.results.sequence
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import java.time.ZoneOffset

val Cohort = createApplicationPlugin(name = "Cohort", createConfiguration = ::CohortConfiguration) {

  application.routing {
    this@createApplicationPlugin.pluginConfig.apply {

      if (heapDump) {
        get("cohort/heapdump") {
          getHeapDump().fold(
            { call.respond(HttpStatusCode.OK, it) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      dataSources.let { dsm ->
        if (dsm.isNotEmpty()) {
          get("cohort/datasources") {
            dsm.map { it.info() }.sequence().fold(
              { call.respondText(mapper.writeValueAsString(it), ContentType.Application.Json, HttpStatusCode.OK) },
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

      migrations?.let { m ->
        get("cohort/dbmigration") {
          m.migrations().fold(
            { call.respondText(mapper.writeValueAsString(it), ContentType.Application.Json, HttpStatusCode.OK) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      logManager?.let { manager ->

        data class LogInfo(val levels: List<String>, val loggers: List<Logger>)

        get("cohort/logging") {
          runCatching {
            val levels = manager.levels()
            val loggers = manager.loggers()
            mapper.writeValueAsString(LogInfo(levels, loggers))
          }.fold(
            { call.respondText(it, ContentType.Application.Json, HttpStatusCode.OK) },
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

      if (jvmInfo) {
        get("cohort/jvm") {
          getJvmDetails().fold(
            { call.respondText(mapper.writeValueAsString(it), ContentType.Application.Json, HttpStatusCode.OK) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      if (gc) {
        get("cohort/gc") {
          gcinfo().fold(
            { call.respondText(mapper.writeValueAsString(it), ContentType.Application.Json, HttpStatusCode.OK) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      if (threadDump) {
        get("cohort/threaddump") {
          getThreadDump().fold(
            { call.respondText(it, ContentType.Text.Plain, HttpStatusCode.OK) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      if (sysprops) {
        get("cohort/sysprops") {
          getSysProps().fold(
            { call.respondText(mapper.writeValueAsString(it), ContentType.Application.Json, HttpStatusCode.OK) },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      if (operatingSystem) {
        get("cohort/os") {
          getOperatingSystem().fold(
            {
              val json = mapper.writeValueAsString(it)
              call.respondText(json, ContentType.Application.Json, HttpStatusCode.OK)
            },
            { call.respondText(it.stackTraceToString(), ContentType.Text.Plain, HttpStatusCode.InternalServerError) },
          )
        }
      }

      healthchecks.forEach { (endpoint, registry) ->
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
        }
      }
    }
  }
}

class CohortConfiguration {

  val healthchecks = mutableMapOf<String, HealthCheckRegistry>()

  // set to true to enable the /cohort/heapdump endpoint which will generate a heapdump in hprof format
  var heapDump: Boolean = false

  // set to true to enable the /cohort/os endpoint which returns operating system information
  var operatingSystem: Boolean = false

  var logManager: LogManager? = null

  var dataSources: List<DataSourceManager> = emptyList()

  var migrations: DatabaseMigrationManager? = null

  // set to true to enable the /cohort/jvm endpoint which returns JVM information
  var jvmInfo: Boolean = false

  // set to true to enable the /cohort/gc endpoint which returns garbage collector times and counts
  var gc: Boolean = false

  // set to true to enable the /cohort/threaddump endpoint which returns a thread dump
  var threadDump: Boolean = false

  // set to true to enable the /cohort/sysprops endpoint which returns current system properties
  var sysprops: Boolean = false

  fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
    healthchecks[endpoint] = registry
  }
}
