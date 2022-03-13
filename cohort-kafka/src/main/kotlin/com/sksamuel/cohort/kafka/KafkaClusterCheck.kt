package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties
import java.util.concurrent.TimeUnit

data class KafkaClusterConfig(
  val bootstrapServers: String,
  val ssl: Boolean
)

/**
 * A [Check] that checks that a connection can be made to a kafka cluster.
 */
class KafkaClusterCheck(private val config: KafkaClusterConfig) : Check {

  private val props = Properties().apply {
    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServers
    if (config.ssl) this[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
  }

  override suspend fun check(): CheckResult {
    return try {
      val client = AdminClient.create(props)
      val controller = client.describeCluster().controller().get(1, TimeUnit.MINUTES)
      if (controller.host() != null)
        CheckResult.Healthy("Connected to kafka cluster at ${config.bootstrapServers}")
      else
        CheckResult.Healthy("Kafka cluster returned without controller at ${config.bootstrapServers}")
    } catch (t: Throwable) {
      CheckResult.Unhealthy("Could not connect to kafka cluster at ${config.bootstrapServers}", t)
    }
  }
}
