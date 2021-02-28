package com.sksamuel.healthcheck.kafka

import com.sksamuel.healthcheck.HealthCheck
import com.sksamuel.healthcheck.HealthCheckResult
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * A [HealthCheck] that checks that a topic exists on a kafka cluster.
 */
class KafkaTopicHealthCheck(
  private val config: KafkaClusterConfig,
  private val topic: String
) : HealthCheck {

  private val props = Properties().apply {
    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServers
    if (config.ssl) this[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
  }

  override suspend fun check(): HealthCheckResult {
    val client = AdminClient.create(props)
    val desc = client.describeTopics(listOf(topic)).all().get(1, TimeUnit.MINUTES)[topic]
    return if (desc == null)
      HealthCheckResult.Unhealthy("Topic $topic does not exist on kafka cluster ${config.bootstrapServers}", null)
    else
      HealthCheckResult.Healthy("Kafka topic $topic confirmed (${desc.partitions()} partitions)")
  }
}
