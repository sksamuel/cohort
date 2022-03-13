package com.sksamuel.cohort.elastic

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import org.apache.http.HttpHost
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.cluster.health.ClusterHealthStatus

class ElasticClusterCheck(
  private val hosts: List<HttpHost>,
  private val errorOnYellow: Boolean = false
) : Check {

  override fun check(): CheckResult {
    return try {
      val client = RestHighLevelClient(RestClient.builder(*hosts.toTypedArray()))
      val resp = client.cluster().health(Requests.clusterHealthRequest(), RequestOptions.DEFAULT)
      val status: ClusterHealthStatus = resp.status
      val msg = "Elastic cluster is ${status.name}"
      when (status) {
        ClusterHealthStatus.GREEN -> CheckResult.Healthy(msg)
        ClusterHealthStatus.RED -> CheckResult.Unhealthy(msg, null)
        ClusterHealthStatus.YELLOW -> when (errorOnYellow) {
          true -> CheckResult.Healthy(msg)
          false -> CheckResult.Unhealthy(msg, null)
        }
      }
    } catch (t: Throwable) {
      CheckResult.Unhealthy("Error querying elastic cluster status", t)
    }
  }
}
