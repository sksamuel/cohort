@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.sksamuel.cohort.elastic

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.elasticsearch.client.RestHighLevelClient

/**
 * A cohort [HealthCheck] which executes an arbitrary query against an elastic cluster.
 *
 * @param client the high level rest client to use to connect.
 * @param command the command to execute against the cluster. Return a [HealthCheckResult] to indicate health.
 */
class ElasticClusterCommandCheck(
  private val client: RestHighLevelClient,
  private val command: (RestHighLevelClient) -> HealthCheckResult,
  override val name: String = "elastic_cluster",
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return try {
      runInterruptible(Dispatchers.IO) {
        command(client)
      }
    } catch (c: CancellationException) {
      // Let parent-scope cancellation propagate; runInterruptible may surface it.
      throw c
    } catch (t: Throwable) {
      HealthCheckResult.unhealthy("Error executing health check against elasticsearch", t)
    }
  }
}
