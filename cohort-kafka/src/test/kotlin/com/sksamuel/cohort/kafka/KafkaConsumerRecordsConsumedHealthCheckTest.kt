package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.assertions.nondeterministic.continually
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.withClue
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
import io.kotest.extensions.testcontainers.kafka.consumer
import io.kotest.extensions.testcontainers.kafka.producer
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Bytes
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class KafkaConsumerRecordsConsumedHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("health check should pass while consumer has not yet consumed") {

      kafka.admin().use { it.createTopics(listOf(NewTopic("mytopic1", 1, 1))).all().get() }
      val consumer = kafka.consumer()
      val healthcheck = KafkaConsumerRecordsConsumedHealthCheck(consumer, 5)
      continually(5.seconds) {
         healthcheck.check().status shouldBe HealthStatus.Healthy
         delay(100.milliseconds)
      }
   }

   test("health check should pass while rate is above threshold") {

      kafka.admin().use { it.createTopics(listOf(NewTopic("mytopic2", 1, 1))).all().get() }

      val producer = kafka.producer()
      val consumer = kafka.consumer()
      consumer.subscribe(listOf("mytopic2"))

      val job = launch {
         while (isActive) {
            delay(10)
            producer.send(ProducerRecord("mytopic2", Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
         }
      }

      val healthcheck = KafkaConsumerRecordsConsumedHealthCheck(consumer, 5)
      eventually(5.seconds) {
         consumer.poll(Duration.ofMillis(100))
         val result = healthcheck.check()
         withClue(result) {
            result.status shouldBe HealthStatus.Healthy
         }
         delay(250.milliseconds)
      }

      job.cancel()
      producer.close()
      consumer.close()
   }
})
