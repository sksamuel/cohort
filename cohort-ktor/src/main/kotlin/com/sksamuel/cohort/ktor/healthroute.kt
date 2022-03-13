package com.sksamuel.cohort.ktor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sksamuel.cohort.HealthCheckRegistry
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get

data class ResultJson(
  val name: String,
  val healthy: Boolean,
  val lastCheck: String,
  val message: String?,
  val cause: String?,
  val consecutiveSuccesses: Int,
  val consecutiveFailures: Int,
)

val mapper = jacksonObjectMapper()

fun Route.healthcheck(registry: HealthCheckRegistry, path: String = "health") {
  get(path) {

    val status = registry.status()
    val results = status.results.map {
      ResultJson(
        name = it.key,
        healthy = it.value.healthy,
        lastCheck = it.value.timestamp.toLocalDateTime().toString(),
        message = it.value.result.message,
        cause = it.value.result.cause?.stackTraceToString(),
        consecutiveSuccesses = it.value.consecutiveSuccesses,
        consecutiveFailures = it.value.consecutiveFailures,
      )
    }
    val json = mapper.writeValueAsString(results)

    val code = when (status.healthy) {
      true -> HttpStatusCode.OK
      false -> HttpStatusCode.ServiceUnavailable
    }

    call.respondText(json, ContentType.Application.Json, code)
  }
}
