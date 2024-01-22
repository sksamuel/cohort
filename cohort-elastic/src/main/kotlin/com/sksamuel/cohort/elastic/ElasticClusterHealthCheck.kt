@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.sksamuel.cohort.elastic

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.cluster.health.ClusterHealthStatus

/**
 * A [HealthCheck] which checks the state of the cluster and returns unhealthy
 * if the cluster is red (or yellow if [errorOnYellow] is set to true)
 *
 * @param client the high level rest client to use to connect
 */
class ElasticClusterHealthCheck(
  private val client: RestHighLevelClient,
  private val errorOnYellow: Boolean = false,
  override val name: String = "elastic_cluster_health",
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return runCatching {

      val health = withContext(Dispatchers.IO) {
        client.cluster().health(ClusterHealthRequest(), RequestOptions.DEFAULT)
      }

      val status = health.status
      val msg = "Elastic cluster is ${status.name}"
      when (status) {
        ClusterHealthStatus.RED -> HealthCheckResult.unhealthy(msg, null)
        ClusterHealthStatus.GREEN -> HealthCheckResult.healthy(msg)
        ClusterHealthStatus.YELLOW -> when (errorOnYellow) {
          false -> HealthCheckResult.healthy(msg)
          true -> HealthCheckResult.unhealthy(msg, null)
        }
      }

    }.getOrElse {
      HealthCheckResult.unhealthy("Error connecting to elastic", it)
    }
  }
}
