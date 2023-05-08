package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod

enum class Method {
   GET,
   POST,
   PUT,
   DELETE,
   PATCH,
   HEAD,
   OPTIONS
}

class HttpHealthCheck(
   private val url: String,
   method: Method = Method.GET,
   private val body: ByteArray? = null,
   private val headers: Map<String, String> = emptyMap(),
   private val successCodes: Set<Int> = setOf(200, 201, 202, 203, 204, 205),
) : HealthCheck {

   override val name: String = "http_call"

   private val client = HttpClient(Apache) {
      expectSuccess = false
   }

   private val ktorMethod = when (method) {
      Method.DELETE -> HttpMethod.Delete
      Method.GET -> HttpMethod.Get
      Method.POST -> HttpMethod.Head
      Method.PUT -> HttpMethod.Put
      Method.PATCH -> HttpMethod.Patch
      Method.HEAD -> HttpMethod.Head
      Method.OPTIONS -> HttpMethod.Options
   }

   override suspend fun check(): HealthCheckResult {

      val resp = client.request(url) {
         this.method = ktorMethod
         this@HttpHealthCheck.body?.let { setBody(it) }
         this@HttpHealthCheck.headers.forEach { (k, v) -> header(k, v) }
      }

      return if (successCodes.contains(resp.status.value)) {
         HealthCheckResult.healthy("Url $url returned ${resp.status}")
      } else
         HealthCheckResult.unhealthy("Url $url returned ${resp.status}", null)
   }
}
