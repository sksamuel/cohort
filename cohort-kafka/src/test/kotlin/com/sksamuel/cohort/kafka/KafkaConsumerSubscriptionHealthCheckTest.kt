package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
import io.kotest.extensions.testcontainers.kafka.consumer
import io.kotest.extensions.testcontainers.kafka.producer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Bytes
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds

class KafkaConsumerSubscriptionHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("health check should pass while the consumer is subscribed") {

      kafka.admin().createTopics(listOf(NewTopic("topic1234", 1, 1))).all().get()
      val consumer = kafka.consumer()
      consumer.subscribe(listOf("topic1234"))

      val healthcheck = KafkaConsumerSubscriptionHealthCheck(consumer)
      healthcheck.check().status shouldBe HealthStatus.Healthy

      consumer.close()
      healthcheck.check().status shouldBe HealthStatus.Unhealthy
   }

   test("health check should support running on the kafka thread") {

      kafka.admin().createTopics(listOf(NewTopic("topic4412", 1, 1))).all().get()
      val consumer = kafka.consumer()
      kafka.producer().use {
         it.send(ProducerRecord("topic4412", Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
      }

      consumer.subscribe(listOf("topic4412"))

      val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
      val job = launch(dispatcher) {
         while (isActive) {
            consumer.poll(Duration.ofMillis(100)) // keep a lock on the consumer
            delay(100)
         }
      }
      job.invokeOnCompletion { consumer.close() }

      val healthcheck = KafkaConsumerSubscriptionHealthCheck(consumer, dispatcher)
      continually(3.seconds) {
         healthcheck.check().status shouldBe HealthStatus.Healthy
      }

      job.cancel()

      eventually(3.seconds) {
         healthcheck.check().status shouldBe HealthStatus.Unhealthy
      }
   }

})
