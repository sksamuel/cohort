package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
import io.kotest.extensions.testcontainers.kafka.consumer
import io.kotest.extensions.testcontainers.kafka.producer
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.utils.Bytes
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class KafkaConsumersHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("KafkaConsumersHealthCheck should pass while a consumer has not yet started") {

      val topic = "topic" + Random.nextInt()
      kafka.admin().createTopics(listOf(NewTopic(topic, 1, 1))).all().get()
      val consumer = kafka.consumer {
         this[ConsumerConfig.GROUP_ID_CONFIG] = "consumer" + Random.nextInt()
         this[ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG] = CountingConsumerInterceptor::class.java.name
      }

      val healthcheck = KafkaConsumersHealthCheck()
      healthcheck.check().status shouldBe HealthStatus.Healthy

      delay(2.seconds)

      healthcheck.check().status shouldBe HealthStatus.Healthy
      consumer.close()
   }

   test("KafkaConsumersHealthCheck should be healthy while consuming records") {

      val topic = "topic" + Random.nextInt()
      kafka.admin().createTopics(listOf(NewTopic(topic, 1, 1))).all().get()
      val consumer = kafka.consumer {
         this[ConsumerConfig.GROUP_ID_CONFIG] = "consumer" + Random.nextInt()
         this[ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG] = CountingConsumerInterceptor::class.java.name
      }

      val producer = kafka.producer()
      val job1 = launch(Dispatchers.IO) {
         while (isActive) {
            delay(10)
            producer.send(ProducerRecord(topic, Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
         }
      }

      val healthcheck = KafkaConsumersHealthCheck()
      healthcheck.check().status shouldBe HealthStatus.Healthy

      consumer.subscribe(listOf(topic))
      consumer.poll(Duration.ofMillis(1000)).shouldHaveAtLeastSize(1)
      healthcheck.check().status shouldBe HealthStatus.Healthy

      delay(1000)
      consumer.poll(Duration.ofMillis(1000)).shouldHaveAtLeastSize(1)
      healthcheck.check().status shouldBe HealthStatus.Healthy

      delay(1000)
      healthcheck.check().status shouldBe HealthStatus.Unhealthy

      job1.cancel()
      consumer.close()
   }

   test("KafkaConsumersHealthCheck should be unhealthy when stalled") {

      val topic = "topic" + Random.nextInt()
      val groupId = "consumer" + Random.nextInt()

      kafka.admin().createTopics(listOf(NewTopic(topic, 1, 1))).all().get()
      val consumer = kafka.consumer {
         this[ConsumerConfig.GROUP_ID_CONFIG] = groupId
         this[ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG] = CountingConsumerInterceptor::class.java.name
      }

      val producer = kafka.producer()
      val job1 = launch(Dispatchers.IO) {
         while (isActive) {
            delay(10)
            producer.send(ProducerRecord(topic, Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
         }
      }

      val healthcheck = KafkaConsumersHealthCheck()
      healthcheck.check().status shouldBe HealthStatus.Healthy

      consumer.subscribe(listOf(topic))
      consumer.poll(Duration.ofMillis(1000)).shouldHaveAtLeastSize(1)
      healthcheck.check().status shouldBe HealthStatus.Healthy

      delay(1000)
      consumer.poll(Duration.ofMillis(1000)).shouldHaveAtLeastSize(1)
      healthcheck.check().status shouldBe HealthStatus.Healthy

      delay(1000)
      healthcheck.check().status shouldBe HealthStatus.Unhealthy
      healthcheck.check().message shouldContain "Kakfa consumers are stalled on: $groupId"

      job1.cancel()
      consumer.close()
   }
})
