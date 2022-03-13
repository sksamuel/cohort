package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.Check
import com.sksamuel.cohort.CheckResult
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * A [Check] that checks that a topic exists on a kafka cluster.
 */
class KafkaTopicCheck(
  private val config: KafkaClusterConfig,
  private val topic: String
) : Check {

  private val props = Properties().apply {
    this[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServers
    if (config.ssl) this[AdminClientConfig.SECURITY_PROTOCOL_CONFIG] = "SSL"
  }

  override fun check(): CheckResult {
    val client = AdminClient.create(props)
    val desc = client.describeTopics(listOf(topic)).all().get(1, TimeUnit.MINUTES)[topic]
    return if (desc == null)
      CheckResult.Unhealthy("Topic $topic does not exist on kafka cluster ${config.bootstrapServers}", null)
    else
      CheckResult.Healthy("Kafka topic $topic confirmed (${desc.partitions().size} partitions)")
  }
}
