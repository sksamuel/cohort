package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer

/**
 * A [HealthCheck] that checks that a kafka consumer is consuming messages.
 * This check can be useful to detect stalled consumers.
 *
 * This check reports healthy if the records consumed between invocations is >= 1.
 */
class KafkaConsumerInactivityHealthCheck(
   consumer: KafkaConsumer<*, *>,
) : HealthCheck {

   override val name: String = "kafka_consumer_inactivity"
   private val check = KafkaConsumerCountHealthCheck(consumer, 1)

   override suspend fun check(): HealthCheckResult {
      return check.check()
   }
}
