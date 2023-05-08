package com.sksamuel.cohort.mongo

import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.rabbit.RabbitConnectionHealthCheck
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerExtension
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

class RabbitConnectionHealthCheckTest : FunSpec({

  val container = RabbitMQContainer(DockerImageName.parse("rabbitmq"))
  install(TestContainerExtension(container))

  test("RabbitConnectionHealthCheck should connect to RabbitMQ") {
    val connection = ConnectionFactory().apply {
      host = container.host
      port = container.amqpPort
    }
    RabbitConnectionHealthCheck(connection).check() shouldBe HealthCheckResult.Healthy("Connected to rabbit instance")
  }

  test("RabbitConnectionHealthCheck should fail if cannot connect") {
    val connection = ConnectionFactory().apply {
      host = container.host
      port = 12312
    }
    RabbitConnectionHealthCheck(connection).check()
      .message.shouldBe("Could not connect to rabbit instance")
  }

})
