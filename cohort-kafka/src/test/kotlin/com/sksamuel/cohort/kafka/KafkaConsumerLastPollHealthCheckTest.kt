//package com.sksamuel.cohort.kafka
//
//import com.sksamuel.cohort.HealthStatus
//import io.kotest.assertions.nondeterministic.continually
//import io.kotest.core.extensions.install
//import io.kotest.core.spec.style.FunSpec
//import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
//import io.kotest.extensions.testcontainers.kafka.admin
//import io.kotest.extensions.testcontainers.kafka.consumer
//import io.kotest.extensions.testcontainers.kafka.producer
//import io.kotest.matchers.shouldBe
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import org.apache.kafka.clients.admin.NewTopic
//import org.apache.kafka.clients.producer.ProducerRecord
//import org.apache.kafka.common.utils.Bytes
//import org.testcontainers.containers.KafkaContainer
//import org.testcontainers.utility.DockerImageName
//import java.time.Duration
//import kotlin.time.Duration.Companion.milliseconds
//import kotlin.time.Duration.Companion.seconds
//
//class KafkaConsumerLastPollHealthCheckTest : FunSpec() {
//   init {
//
//      val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))
//
//      test("health check should pass while the consumer has not yet started") {
//
//         val consumer = kafka.consumer()
//         val healthcheck = KafkaConsumerLastPollHealthCheck(consumer, 1.seconds)
//
//         continually(5.seconds) {
//            healthcheck.check().status shouldBe HealthStatus.Healthy
//            delay(250.milliseconds)
//         }
//
//         consumer.metrics().values.toList().sortedBy { it.metricName().name() }
//            .forEach { println("${it.metricName()} = ${it.metricValue()}") }
//
//         consumer.close()
//      }
//
//      test("health check should pass while the consumer is active") {
//
//         kafka.admin().use { it.createTopics(listOf(NewTopic("mytopic2", 1, 1))).all().get() }
//
//         val producer = kafka.producer()
//         val consumer = kafka.consumer()
//         consumer.subscribe(listOf("mytopic2"))
//
//         val job = launch {
//            while (isActive) {
//               delay(10)
//               producer.send(ProducerRecord("mytopic2", Bytes.wrap(byteArrayOf()), Bytes.wrap(byteArrayOf())))
//            }
//         }
//
//         val healthcheck = KafkaConsumerLastPollHealthCheck(consumer, 1.seconds)
//         continually(5.seconds) {
//            consumer.poll(Duration.ofMillis(100))
//            healthcheck.check().status shouldBe HealthStatus.Healthy
//            delay(250.milliseconds)
//         }
//         delay(2.seconds)
//         healthcheck.check().status shouldBe HealthStatus.Unhealthy
//
//         consumer.metrics().values.toList().sortedBy { it.metricName().name() }
//            .forEach { println("${it.metricName()} = ${it.metricValue()}") }
//
//         job.cancel()
//         producer.close()
//         consumer.close()
//      }
//   }
//}
