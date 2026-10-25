package com.sksamuel.cohort.clickhouse

import com.clickhouse.client.api.Client
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class ClickHouseConnectionHealthCheckContainerTest : FunSpec({

   val httpPort = 8123
   val username = "cohort"
   val password = "cohort"

   val clickhouse = GenericContainer(DockerImageName.parse("clickhouse/clickhouse-server:24.8")).apply {
      withExposedPorts(httpPort)
      withEnv("CLICKHOUSE_USER", username)
      withEnv("CLICKHOUSE_PASSWORD", password)
      withEnv("CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT", "1")
      waitingFor(Wait.forHttp("/ping").forPort(httpPort).forStatusCode(200))
   }

   beforeSpec {
      clickhouse.start()
   }

   afterSpec {
      clickhouse.close()
   }

   fun client(endpoint: String): Client =
      Client.Builder()
         .addEndpoint(endpoint)
         .setUsername(username)
         .setPassword(password)
         .build()

   test("healthy when the server responds") {
      client("http://${clickhouse.host}:${clickhouse.getMappedPort(httpPort)}").use { client ->
         ClickHouseConnectionHealthCheck(client).check().status shouldBe HealthStatus.Healthy
      }
   }

   test("unhealthy when the server cannot be reached") {
      client("http://${clickhouse.host}:1").use { client ->
         ClickHouseConnectionHealthCheck(client).check().status shouldBe HealthStatus.Unhealthy
      }
   }
})
