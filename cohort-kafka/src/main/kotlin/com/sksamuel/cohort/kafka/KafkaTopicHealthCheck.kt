package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException

/**
 * A [HealthCheck] that checks that a topic exists on a kafka cluster.
 */
class KafkaTopicHealthCheck(
   private val admin: Admin,
   private val topic: String,
   override val name: String = "kafka_topic",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      return try {
         val descriptions = admin.describeTopics(listOf(topic)).allTopicNames().toCompletionStage().await()
         val description = descriptions[topic]
         if (description == null) {
            HealthCheckResult.unhealthy("Topic $topic does not exist on kafka cluster", null)
         } else {
            HealthCheckResult.healthy("Kafka topic $topic confirmed (${description.partitions().size} partitions)")
         }
      } catch (e: UnknownTopicOrPartitionException) {
         HealthCheckResult.unhealthy("Topic $topic does not exist on kafka cluster", e)
      } catch (t: Throwable) {
         HealthCheckResult.unhealthy("Could not query kafka cluster for topic $topic", t)
      }
   }
}
