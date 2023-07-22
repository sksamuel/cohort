package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * A Cohort [HealthCheck] that checks for stalled consumers by retrieving the committed offsets
 * for a given consumer group. If the commited offset is the same, for all topics, between health
 * check invocations, this check returns unhealthy.
 *
 * In other words, at least one partition must have an advancing offset for the given consumer group
 * between health check invocations.
 *
 * This check can be useful to detect consumer groups which are making no progress.
 */
class KafkaConsumerGroupOffsetHealthCheck(
   private val admin: Admin,
   private val consumerGroupId: String,
) : HealthCheck {

   private var offsets = mapOf<TopicPartition, OffsetAndMetadata>()

   override suspend fun check(): HealthCheckResult {

      val groups = admin.listConsumerGroupOffsets(consumerGroupId).all().toCompletionStage().await()

      val newOffsets = groups[consumerGroupId] ?: return HealthCheckResult.unhealthy(
         "Offset metadata not available for consumer group $consumerGroupId",
         null
      )

      // we are healthy, if for any partition, this is the first time we see the partition,
      // or we have advanced the offset
      val healthy = newOffsets.any { (tp, offset) ->
         val currentOffset = offsets[tp]
         currentOffset == null || currentOffset.offset() < offset.offset()
      }

      // replace all current offsets so next check uses new values
      offsets = newOffsets

      return if (healthy) {
         HealthCheckResult.healthy("Kakfa consumer group is making progress")
      } else {
         HealthCheckResult.unhealthy("Kakfa consumer group has stalled", null)
      }
   }
}
