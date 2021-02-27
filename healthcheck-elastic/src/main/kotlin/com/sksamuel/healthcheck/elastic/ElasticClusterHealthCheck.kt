package com.sksamuel.healthcheck.elastic

import com.sksamuel.healthcheck.HealthCheck
import com.sksamuel.healthcheck.HealthCheckResult
import org.apache.http.HttpHost
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient

class ElasticClusterHealthCheck(private val hosts: List<HttpHost>) : HealthCheck {

  private val request = Request("GET", "/")

  override fun check(): HealthCheckResult {
    return try {
      val client = RestClient.builder(*hosts.toTypedArray()).build()
      val response = client.performRequest(request)
      return when (response.statusLine.statusCode) {
        200 -> HealthCheckResult.Healthy("Connected to elastic cluster at $hosts")
        else -> HealthCheckResult.Unhealthy("Check returned status ${response.statusLine}", null)
      }
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error performing elastic check", t)
    }
  }
}
