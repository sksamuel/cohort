package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.either.Either
import com.sksamuel.tabby.either.left
import com.sksamuel.tabby.either.right
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.Metric

/**
 * Kafka metrics can subclass this class and use the provided methods to retrieve metrics.
 */
abstract class KafkaConsumerMetricHealthCheck(private val consumer: KafkaConsumer<*, *>) : HealthCheck {

   protected fun metric(name: String): Either<HealthCheckResult, Metric> =
      consumer.metrics().values.firstOrNull { it.metricName().name() == name }?.right()
         ?: HealthCheckResult.unhealthy("Could not locate kafka metric '${name}'", null).left()

}
