package com.sksamuel.cohort.rabbit

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.TestContainerSpecExtension
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

class RabbitQueueHealthCheckTest : FunSpec({

   val container = RabbitMQContainer(DockerImageName.parse("rabbitmq"))
   install(TestContainerSpecExtension(container))

   fun factory() = ConnectionFactory().apply {
      host = container.host
      port = container.amqpPort
   }

   beforeSpec {
      // Create the queue used in the healthy tests
      factory().newConnection().use { conn ->
         conn.createChannel().use { ch ->
            ch.queueDeclare("test-queue", false, false, false, null)
         }
      }
   }

   test("returns healthy when queue exists") {
      RabbitQueueHealthCheck(factory(), "test-queue").check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when queue does not exist") {
      RabbitQueueHealthCheck(factory(), "no-such-queue").check().status shouldBe HealthStatus.Unhealthy
   }

   test("connection and channel are closed after a successful check") {
      val createdConnections = mutableListOf<Connection>()
      val createdChannels = mutableListOf<Channel>()

      val trackingFactory = object : ConnectionFactory() {
         override fun newConnection(): Connection {
            val conn = object : TrackingConnection(super.newConnection()) {
               override fun createChannel(): Channel {
                  return super.createChannel().also { createdChannels += it }
               }
            }
            createdConnections += conn
            return conn
         }
      }.apply {
         host = container.host
         port = container.amqpPort
      }

      RabbitQueueHealthCheck(trackingFactory, "test-queue").check()

      createdConnections.size shouldBe 1
      createdChannels.size shouldBe 1
      createdConnections[0].isOpen shouldBe false
      createdChannels[0].isOpen shouldBe false
   }
})

private open class TrackingConnection(private val delegate: Connection) : Connection by delegate {
   override fun createChannel(): Channel = delegate.createChannel()
}
