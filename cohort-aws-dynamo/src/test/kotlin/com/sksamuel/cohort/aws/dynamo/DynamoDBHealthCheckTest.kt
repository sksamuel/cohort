package com.sksamuel.cohort.aws.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DynamoDBHealthCheckTest : FunSpec({

   test("should be healthy if listTables succeeds") {
      val client = mockk<AmazonDynamoDB>()
      every { client.listTables(any<Int>()) } returns null
      val check = DynamoDBHealthCheck(client)
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 0) { client.shutdown() }
   }

   test("should be unhealthy if listTables fails") {
      val client = mockk<AmazonDynamoDB>()
      every { client.listTables(any<Int>()) } throws RuntimeException("foo")
      val check = DynamoDBHealthCheck(client)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 0) { client.shutdown() }
   }
})
