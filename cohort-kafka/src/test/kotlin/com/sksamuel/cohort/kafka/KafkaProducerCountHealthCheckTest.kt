package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
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
import kotlin.time.Duration.Companion.seconds

class KafkaProducerCountHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("health check should pass while the consumer is active") {

      kafka.admin().use { it.createTopics(listOf(NewTopic("mytopic", 1, 1))).all().get() }
      val producer = kafka.producer()

      val job = launch {
         while (isActive) {
            delay(10)
            producer.send(ProducerRecord("mytopic", Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
         }
      }

      val healthcheck = KafkaProducerCountHealthCheck(producer, 80)
      delay(1.seconds) // should have sent ~ 100
      healthcheck.check().status shouldBe HealthStatus.Healthy
      job.cancel()
      delay(1.seconds) // now should be zero
      healthcheck.check().status shouldBe HealthStatus.Unhealthy

      producer.close()
   }
})
