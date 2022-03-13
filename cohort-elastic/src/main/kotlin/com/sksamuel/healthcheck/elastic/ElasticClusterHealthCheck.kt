package com.sksamuel.healthcheck.elastic

import com.sksamuel.healthcheck.HealthCheck
import com.sksamuel.healthcheck.HealthCheckResult
import org.apache.http.HttpHost
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.cluster.health.ClusterHealthStatus

class ElasticClusterHealthCheck(
  private val hosts: List<HttpHost>,
  private val errorOnYellow: Boolean = false
) : HealthCheck {

  override fun check(): HealthCheckResult {
    return try {
      val client = RestHighLevelClient(RestClient.builder(*hosts.toTypedArray()))
      val resp = client.cluster().health(Requests.clusterHealthRequest(), RequestOptions.DEFAULT)
      val status: ClusterHealthStatus = resp.status
      val msg = "Elastic cluster is ${status.name}"
      when (status) {
        ClusterHealthStatus.GREEN -> HealthCheckResult.Healthy(msg)
        ClusterHealthStatus.RED -> HealthCheckResult.Unhealthy(msg, null)
        ClusterHealthStatus.YELLOW -> when (errorOnYellow) {
          true -> HealthCheckResult.Healthy(msg)
          false -> HealthCheckResult.Unhealthy(msg, null)
        }
      }
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error querying elastic cluster status", t)
    }
  }
}
