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
  private val bootstrapServers: String,
  private val ssl: Boolean,
  private val topic: String
) : HealthCheck {
  override fun check(): HealthCheckResult {
    val props = Properties()
    props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    if (ssl) props[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
    return try {
      val client = AdminClient.create(props)
      val desc = client.describeTopics(listOf(topic)).all().get(1, TimeUnit.MINUTES)[topic]
      if (desc == null)
        HealthCheckResult.Unhealthy("Topic $topic does not exist on kafka cluster $bootstrapServers", null)
      else
        HealthCheckResult.Healthy("Kafka topic $topic confirmed exists (${desc.partitions()} partitions)")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Could not connect to kafka cluster at $bootstrapServers", t)
    }
  }
}
