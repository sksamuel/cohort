package com.sksamuel.cohort.mongo

import com.mongodb.client.MongoClients
import com.sksamuel.cohort.HealthCheckResult
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoConnectionHealthCheckTest : FunSpec({

  val container = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
  install(TestContainerExtension(container))

  test("mongo health check should connect to mongo") {
    val client = MongoClients.create(container.connectionString)
    MongoConnectionHealthCheck(client).check() shouldBe HealthCheckResult.Healthy("Connected to mongo instance (3 databases)")
  }

  test("mongo health check should fail if cannot connect") {
    val client = MongoClients.create("mongodb://localhost:11111")
    MongoConnectionHealthCheck(client).check().message.shouldBe("Could not connect to mongo instance")
  }

})
