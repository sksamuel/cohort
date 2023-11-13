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
abstract class AbstractKafkaConsumerMetricHealthCheck(private val consumer: KafkaConsumer<*, *>) : HealthCheck {

   /**
    * Returns the metric with the given [name] that has the least number of tags (most generic metric value).
    */
   protected fun metric(name: String): Either<HealthCheckResult, Metric> {
      return consumer.metrics().values
         .filter { it.metricName().name() == name }
         .minByOrNull { it.metricName().tags().size }?.right()
         ?: HealthCheckResult.unhealthy("Could not locate kafka metric '${name}'", null).left()
   }


   /**
    * Returns the metric with the given [name] that has the least number of tags (most generic metric value).
    */
   protected fun metricOrNull(name: String): Metric? {
      return consumer.metrics().values
         .filter { it.metricName().name() == name }
         .minByOrNull { it.metricName().tags().size }
   }
}
