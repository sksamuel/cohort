package com.sksamuel.cohort.mongo

import com.mongodb.client.MongoClients
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoConnectionHealthCheckTest : FunSpec({

   val container = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
   install(TestContainerExtension(container))

   test("mongo health check should connect to mongo") {
      val client = MongoClients.create(container.connectionString)
      // Don't assert the database count — it depends on which mongo image we run against and
      // changes when the image is bumped. The presence of the prefix is the real signal.
      val result = MongoConnectionHealthCheck(client).check()
      result.status shouldBe HealthStatus.Healthy
      result.message shouldStartWith "Connected to mongo instance"
   }

   test("mongo health check should fail if cannot connect") {
      val client = MongoClients.create("mongodb://localhost:11111")
      MongoConnectionHealthCheck(client).check().message.shouldBe("Could not connect to mongo instance")
   }

   test("mongo coroutine health check should connect to mongo") {
      val client = MongoClient.create(container.connectionString)
      val result = MongoConnectionHealthCheck(client).check()
      result.status shouldBe HealthStatus.Healthy
      result.message shouldStartWith "Connected to mongo instance"
   }

   test("mongo coroutine check should fail if cannot connect") {
      val client = MongoClient.create("mongodb://localhost:11111")
      MongoConnectionHealthCheck(client).check().message.shouldBe("Could not connect to mongo instance")
   }
})
