package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.Properties
import kotlin.math.roundToInt

@Deprecated("use KafkaProducerRateHealthCheck")
typealias KafkaProducerRecordSendRateHealthCheck = KafkaProducerRateHealthCheck

/**
 * A [HealthCheck] that checks that a kafka producer is sending a minimum number of messages.
 *
 * This check can be useful to detect stalled producers.
 *
 * This check reports healthy if the min send rate is >= [minSendRate].
 */
class KafkaProducerRateHealthCheck(
   private val producer: KafkaProducer<*, *>,
   private val minSendRate: Int,
) : HealthCheck {

   private val metricName = "record-send-rate"

   override val name: String = "kafka_producer_rate"

   override suspend fun check(): HealthCheckResult {
      val metric = producer.metrics().values.firstOrNull { it.metricName().name() == metricName }
         ?: return HealthCheckResult.unhealthy("Could not locate kafka metric '${metricName}'", null)
      val sendRate = metric.metricValue().toString().toDoubleOrNull()?.roundToInt() ?: 0
      val msg = "Kafka producer $metricName $sendRate [min threshold $minSendRate]"
      return if (sendRate < minSendRate)
         HealthCheckResult.unhealthy(msg, null)
      else
         HealthCheckResult.healthy(msg)
   }
}

fun main() {
   val props = Properties()
   props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
   val producer = KafkaConsumer(props, StringDeserializer(), StringDeserializer())
   producer.metrics().toList().sortedBy { it.first.name() }.forEach { (a, metric) -> println(a) }
}
