package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.assertions.nondeterministic.continually
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.Admin
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

class KafkaClusterHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("health check should be able to connect to a kafka cluster") {
      kafka.admin().use { admin ->
         KafkaClusterHealthCheck(admin).check().status shouldBe HealthStatus.Healthy
      }
   }

   test("health checks should fail if unable to connect") {
      val props = Properties()
      props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = "plaintext://localhost:12345"
      Admin.create(props).use { admin ->
         continually(5.seconds) {
            KafkaClusterHealthCheck(admin).check().status shouldBe HealthStatus.Unhealthy
         }
      }
   }
})
