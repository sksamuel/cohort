package com.sksamuel.healthcheck.ktor

import com.sksamuel.healthcheck.HealthCheckRegistry
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.healthcheck(registry: HealthCheckRegistry) {
  get("health") {
    val status = registry.status()
    when (status.healthy) {
      true -> call.respond(HttpStatusCode.OK, status.results)
      false -> call.respond(HttpStatusCode.InternalServerError, status.results)
    }
  }
}
