package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.Admin

/**
 * A [HealthCheck] that checks that a topic exists on a kafka cluster.
 */
class KafkaTopicHealthCheck(
   private val admin: Admin,
   private val topic: String,
   override val name: String = "kafka_topic",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {

      val descriptionMap = runCatching {
         admin.describeTopics(listOf(topic)).allTopicNames().toCompletionStage().await()
      }.getOrElse { emptyMap() }

      val topicDescription = descriptionMap[topic]
      return if (topicDescription == null)
         HealthCheckResult.unhealthy("Topic $topic does not exist on kafka cluster", null)
      else
         HealthCheckResult.healthy("Kafka topic $topicDescription confirmed (${topicDescription.partitions().size} partitions)")
   }
}
