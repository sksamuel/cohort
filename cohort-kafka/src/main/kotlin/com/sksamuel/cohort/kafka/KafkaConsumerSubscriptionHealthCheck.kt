package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer

/**
 * A [HealthCheck] that checks that a kafka consumer is subscribed to either
 * - a specified set of topics
 * - or at least one topic
 *
 * This check can be useful to detect stalled consumers.
 *
 * This check reports healthy if the consumer is subscribed to all the provided topics, or if no
 * topics are provided, then is subscribed to at least one topic.
 */
class KafkaConsumerSubscriptionHealthCheck(
   private val consumer: KafkaConsumer<*, *>,
   private val topics: Set<String>
) : HealthCheck {

   /**
    * Create the health check requiring this consumer to be subscribed to at least one topic, but
    * without specifying what topic that is.
    */
   constructor(consumer: KafkaConsumer<*, *>) : this(consumer, emptySet())

   override val name: String = "kafka_consumer_subscription"

   override suspend fun check(): HealthCheckResult {
      val subs = consumer.subscription()
      val healthy = when {
         topics.isEmpty() -> subs.isNotEmpty()
         else -> subs.toSet().intersect(topics) == topics
      }
      val msg = "Kafka consumer is subscribed to ${subs.size} topics"
      return when (healthy) {
         true -> HealthCheckResult.healthy(msg)
         false -> HealthCheckResult.unhealthy(msg)
      }
   }
}
