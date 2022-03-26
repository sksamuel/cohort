package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties

data class KafkaClusterConfig(
  val bootstrapServers: String,
  val ssl: Boolean
)

/**
 * A [HealthCheck] that checks that a connection can be made to a kafka cluster, the controller
 * can be located, and at least one node is present.
 */
class KafkaClusterHealthCheck(private val config: KafkaClusterConfig) : HealthCheck {

  private val props = Properties().apply {
    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServers
    if (config.ssl) this[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
  }

  override suspend fun check(): HealthCheckResult {
    return try {
      val client = AdminClient.create(props)
      val clusterResult = client.describeCluster()
      val controller = clusterResult.controller().toCompletionStage().await()
      val nodes = clusterResult.nodes().toCompletionStage().await()

      when {
        nodes.isEmpty() -> HealthCheckResult.Unhealthy("Kafka cluster is showing no nodes", null)
        controller == null -> HealthCheckResult.Unhealthy("Kafka cluster returned without controller", null)
        else -> HealthCheckResult.Healthy("Connected to kafka cluster with controller ${controller.host()} and ${nodes.size} node(s)")
      }
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Could not connect to kafka cluster at ${config.bootstrapServers}", t)
    }
  }
}
