package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.KafkaConsumer
import kotlin.math.roundToInt

@Deprecated("use KafkaConsumerRateHealthCheck")
typealias KafkaConsumerRecordsConsumedRateHealthCheck = KafkaConsumerRateHealthCheck

/**
 * A [HealthCheck] that checks that a kafka consumer is consuming a minimum number of messages.
 *
 * This check can be useful to detect stalled consumers.
 *
 * This check reports healthy if the records-consumed-rate is >= [minReceiveRate].
 */
class KafkaConsumerRateHealthCheck(
   private val consumer: KafkaConsumer<*, *>,
   private val minReceiveRate: Int,
) : HealthCheck {

   private val metricName = "records-consumed-rate"

   override val name: String = "kafka_consumer_rate"

   override suspend fun check(): HealthCheckResult {
      val metric = consumer.metrics().values.firstOrNull { it.metricName().name() == metricName }
         ?: return HealthCheckResult.Unhealthy("Could not locate kafka metric '${metricName}'", null)
      val sendRate = metric.metricValue().toString().toDoubleOrNull()?.roundToInt() ?: 0
      val msg = "Kafka consumer $metricName $sendRate [min threshold $minReceiveRate]"
      return if (sendRate < minReceiveRate)
         HealthCheckResult.Unhealthy(msg, null)
      else
         HealthCheckResult.Healthy(msg)
   }
}
