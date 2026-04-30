package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.DescribeTopicsResult
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import java.util.concurrent.CompletableFuture

class KafkaTopicHealthCheckTest : FunSpec({

   fun adminThatReturns(map: Map<String, TopicDescription>): Admin {
      val admin = mockk<Admin>()
      val descResult = mockk<DescribeTopicsResult>()
      every { admin.describeTopics(any<Collection<String>>()) } returns descResult
      every { descResult.allTopicNames() } returns KafkaFuture.completedFuture(map)
      return admin
   }

   fun adminThatThrows(throwable: Throwable): Admin {
      val admin = mockk<Admin>()
      val descResult = mockk<DescribeTopicsResult>()
      val future = mockk<KafkaFuture<Map<String, TopicDescription>>>()
      every { admin.describeTopics(any<Collection<String>>()) } returns descResult
      every { descResult.allTopicNames() } returns future
      every { future.toCompletionStage() } returns CompletableFuture<Map<String, TopicDescription>>().also {
         it.completeExceptionally(throwable)
      }
      return admin
   }

   test("returns healthy when topic exists") {
      val description = mockk<TopicDescription>()
      every { description.partitions() } returns listOf(mockk(), mockk(), mockk())

      val result = KafkaTopicHealthCheck(adminThatReturns(mapOf("mytopic" to description)), "mytopic").check()

      result.status shouldBe HealthStatus.Healthy
      result.message shouldContain "Kafka topic mytopic confirmed"
      result.message shouldContain "3 partitions"
   }

   test("returns unhealthy with not-found message when describe throws UnknownTopicOrPartitionException") {
      val ex = UnknownTopicOrPartitionException("nope")
      val result = KafkaTopicHealthCheck(adminThatThrows(ex), "mytopic").check()

      result.status shouldBe HealthStatus.Unhealthy
      result.message shouldContain "does not exist"
      result.cause shouldBe ex
   }

   test("returns unhealthy with cluster-error message and preserves cause on non-not-found errors") {
      // Connection failure must NOT be reported as "topic does not exist".
      val ex = java.io.IOException("connection refused")
      val result = KafkaTopicHealthCheck(adminThatThrows(ex), "mytopic").check()

      result.status shouldBe HealthStatus.Unhealthy
      result.message shouldContain "Could not query"
      result.cause shouldNotBe null
   }
})
