package com.sksamuel.healthcheck.kafka

import com.sksamuel.healthcheck.HealthCheck
import com.sksamuel.healthcheck.HealthCheckResult
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * A [HealthCheck] that checks that a connection can be made to a kafka cluster.
 */
class KafkaClusterHealthCheck(private val bootstrapServers: String, private val ssl: Boolean) : HealthCheck {
  override fun check(): HealthCheckResult {
    val props = Properties()
    props[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    if (ssl) props[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
    return try {
      val client = AdminClient.create(props)
      val controller = client.describeCluster().controller().get(1, TimeUnit.MINUTES)
      if (controller.host() != null)
        HealthCheckResult.Healthy("Connected to kafka cluster at $bootstrapServers")
      else
        HealthCheckResult.Healthy("Kafka cluster returned without controller at $bootstrapServers")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Could not connect to kafka cluster at $bootstrapServers", t)
    }
  }
}
