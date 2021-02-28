package com.sksamuel.healthcheck.ktor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sksamuel.healthcheck.HealthCheckRegistry
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import java.sql.Timestamp

data class ResultJson(
  val name: String,
  val healthy: Boolean,
  val lastCheck: String,
  val message: String?,
  val cause: String?,
)

val mapper = jacksonObjectMapper()

fun Route.healthcheck(registry: HealthCheckRegistry) {
  get("health") {

    val status = registry.status()
    val results = status.results.map {
      ResultJson(
        name = it.key,
        healthy = it.value.first.isHealthy,
        lastCheck = it.value.second.toLocalDateTime().toString(),
        message = it.value.first.message,
        cause = it.value.first.cause?.stackTraceToString()
      )
    }
    val json = mapper.writeValueAsString(results)

    val code = when (status.healthy) {
      true -> HttpStatusCode.OK
      false -> HttpStatusCode.InternalServerError
    }

    call.respondText(json, ContentType.Application.Json, code)
  }
}
