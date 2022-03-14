@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.sksamuel.cohort.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.HealthStatus
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

/**
 * A [HealthCheck] which checks the state of the cluster and returns unhealthy
 * if the cluster is red, or yellow if [errorOnYellow] is set to true.
 */
class ElasticClusterHealthCheck(
  private val hosts: List<HttpHost>,
  private val errorOnYellow: Boolean = false
) : HealthCheck {

  private val restClient = RestClient.builder(*hosts.toTypedArray()).build()
  private val transport = RestClientTransport(restClient, JacksonJsonpMapper())
  private val client = ElasticsearchClient(transport)

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      val health = client.cluster().health()
      val status = health.status()
      val msg = "Elastic cluster is ${status.name}"
      when (status) {
        HealthStatus.Green -> HealthCheckResult.Healthy(msg)
        HealthStatus.Yellow -> HealthCheckResult.Unhealthy(msg, null)
        HealthStatus.Red -> when (errorOnYellow) {
          true -> HealthCheckResult.Healthy(msg)
          false -> HealthCheckResult.Unhealthy(msg, null)
        }
      }
    }.getOrElse {
      HealthCheckResult.Unhealthy("Error querying elastic cluster status", it)
    }
  }
}
