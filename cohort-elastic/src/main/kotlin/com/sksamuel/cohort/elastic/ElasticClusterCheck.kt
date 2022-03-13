@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.sksamuel.cohort.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.HealthStatus
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

/**
 * A [Check] which checks the state of the cluster and returns unhealthy
 * if the cluster is red, or yellow if [errorOnYellow] is set to true.
 */
class ElasticClusterCheck(
  private val hosts: List<HttpHost>,
  private val errorOnYellow: Boolean = false
) : Check {

  private val restClient = RestClient.builder(*hosts.toTypedArray()).build()
  private val transport = RestClientTransport(restClient, JacksonJsonpMapper())
  private val client = ElasticsearchClient(transport)

  override suspend fun check(): CheckResult {
    return runCatching {
      val health = client.cluster().health()
      val status = health.status()
      val msg = "Elastic cluster is ${status.name}"
      when (status) {
        HealthStatus.Green -> CheckResult.Healthy(msg)
        HealthStatus.Yellow -> CheckResult.Unhealthy(msg, null)
        HealthStatus.Red -> when (errorOnYellow) {
          true -> CheckResult.Healthy(msg)
          false -> CheckResult.Unhealthy(msg, null)
        }
      }
    }.getOrElse {
      CheckResult.Unhealthy("Error querying elastic cluster status", it)
    }
  }
}
