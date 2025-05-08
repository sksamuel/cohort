package com.sksaumel.cohort.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.cassandra.CassandraDriverHealthCheck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName
import java.net.InetSocketAddress

class CassandraDriverHealthCheckContainerTest : FunSpec({

   val network = Network.newNetwork()
   val cassandra = CassandraContainer(DockerImageName.parse("cassandra:5.0")).apply {
      withNetwork(network)
      withNetworkAliases("cassandra")
      withEnv("CASSANDRA_CLUSTER_NAME", "cluster")
      withEnv("CASSANDRA_ENDPOINT_SNITCH", "GossipingPropertyFileSnitch")
      withEnv("CASSANDRA_KEYSPACE", "keyspace")
      withEnv("CASSANDRA_DC", "dc1")
      withEnv("CASSANDRA_RACK", "rack1")
      withExposedPorts(9042)
   }

   beforeSpec {
      cassandra.start()
   }

   afterSpec {
      cassandra.close()
   }

   test("healthy when a node is running") {
      val session = CqlSession.builder()
         .addContactPoint(InetSocketAddress(cassandra.host, cassandra.getMappedPort(9042)))
         .withLocalDatacenter("dc1")
         .build()

      val healthCheck: HealthCheck = CassandraDriverHealthCheck(session)
      healthCheck.check().status shouldBe HealthStatus.Healthy
   }

   test("unhealthy when a node is down") {
      val session = CqlSession.builder()
         .addContactPoint(InetSocketAddress(cassandra.host, cassandra.getMappedPort(9042)))
         .withLocalDatacenter("dc1")
         .build()
      val healthCheck: HealthCheck = CassandraDriverHealthCheck(session)
      // healthy before applying toxic
      healthCheck.check().status shouldBe HealthStatus.Healthy

      // run disablebinary
      cassandra.execInContainer("nodetool", "disablebinary")

      // refresh session
      val unhealthyExpected = runCatching {
         CqlSession.builder()
            .addContactPoint(InetSocketAddress(cassandra.host, cassandra.getMappedPort(9042)))
            .withLocalDatacenter("dc1")
            .build()
            .use {
               CassandraDriverHealthCheck(it).check().status
            }
      }.getOrDefault(HealthStatus.Unhealthy) // AllNodesFailedException
      unhealthyExpected shouldBe HealthStatus.Unhealthy
   }
})
