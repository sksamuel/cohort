package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.admin.AdminClient
import java.util.concurrent.TimeUnit

/**
 * A [HealthCheck] that checks that a topic exists on a kafka cluster.
 */
class KafkaTopicHealthCheck(
  private val adminClient: AdminClient,
  private val topic: String
) : HealthCheck {

  override val name: String = "kafka_topic"

  override suspend fun check(): HealthCheckResult {
    val desc = adminClient.describeTopics(listOf(topic)).all().get(1, TimeUnit.MINUTES)[topic]
    return if (desc == null)
      HealthCheckResult.unhealthy("Topic $topic does not exist on kafka cluster", null)
    else
      HealthCheckResult.healthy("Kafka topic $topic confirmed (${desc.partitions().size} partitions)")
  }
}
