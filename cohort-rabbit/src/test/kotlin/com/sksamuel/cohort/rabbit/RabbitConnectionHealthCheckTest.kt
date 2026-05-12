package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthCheckResult
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerSpecExtension
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

class RabbitConnectionHealthCheckTest : FunSpec({

  val container = RabbitMQContainer(DockerImageName.parse("rabbitmq"))
  install(TestContainerSpecExtension(container))

  test("RabbitConnectionHealthCheck should connect to RabbitMQ") {
    val connection = ConnectionFactory().apply {
      host = container.host
      port = container.amqpPort
    }
    RabbitConnectionHealthCheck(connection).check() shouldBe HealthCheckResult.healthy("Connected to rabbit instance")
  }

  test("RabbitConnectionHealthCheck should fail if cannot connect") {
    val connection = ConnectionFactory().apply {
      host = container.host
      port = 12312
    }
    RabbitConnectionHealthCheck(connection).check()
      .message.shouldBe("Could not connect to rabbit instance")
  }

  test("connection is closed after a successful check") {
    val createdConnections = mutableListOf<Connection>()

    val trackingFactory = object : ConnectionFactory() {
      override fun newConnection(): Connection {
        return super.newConnection().also { createdConnections += it }
      }
    }.apply {
      host = container.host
      port = container.amqpPort
    }

    RabbitConnectionHealthCheck(trackingFactory).check()

    createdConnections.size shouldBe 1
    createdConnections[0].isOpen shouldBe false
  }
})
