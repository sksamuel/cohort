package com.sksamuel.cohort.elastic

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
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

      val health = runInterruptible(Dispatchers.IO) {
        client.cluster().health(ClusterHealthRequest(), RequestOptions.DEFAULT)
      }

      // Treat `health.status` as a platform-nullable enum. The previous file-level
      // @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA") silenced a real concern: a Java-returned
      // null would crash `${status.name}` with NPE, get caught by the outer runCatching, and
      // be reported as "Error connecting to elastic" — misleading, since the connection
      // succeeded. Handle null explicitly.
      val status: ClusterHealthStatus? = health.status
      if (status == null) return@runCatching HealthCheckResult.unhealthy("Elastic cluster returned no status", null)
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
