package com.sksamuel.cohort.aws.sqs

import com.amazonaws.services.sqs.AmazonSQS
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SQSQueueHealthCheckTest : FunSpec({

   test("should be healthy if getQueueUrl succeeds") {
      val client = mockk<AmazonSQS>()
      every { client.getQueueUrl(any<String>()) } returns null
      val check = SQSQueueHealthCheck("myqueue", client)
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 0) { client.shutdown() }
   }

   test("should be unhealthy if getQueueUrl fails") {
      val client = mockk<AmazonSQS>()
      every { client.getQueueUrl(any<String>()) } throws RuntimeException("foo")
      val check = SQSQueueHealthCheck("myqueue", client)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 0) { client.shutdown() }
   }
})
