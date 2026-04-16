package com.sksamuel.cohort.aws.sns

import com.amazonaws.services.sns.AmazonSNS
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SNSHealthCheckTest : FunSpec({

   test("should be healthy if listTopics succeeds") {
      val client = mockk<AmazonSNS>()
      every { client.listTopics() } returns null
      val check = SNSHealthCheck(client)
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 0) { client.shutdown() }
   }

   test("should be unhealthy if listTopics fails") {
      val client = mockk<AmazonSNS>()
      every { client.listTopics() } throws RuntimeException("foo")
      val check = SNSHealthCheck(client)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 0) { client.shutdown() }
   }
})
