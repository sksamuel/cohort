package com.sksaumel.cohort.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.metadata.Metadata
import com.datastax.oss.driver.api.core.metadata.Node
import com.datastax.oss.driver.api.core.metadata.NodeState
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.cassandra.CassandraDriverHealthCheck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

class CassandraDriverHealthCheckTest : FunSpec({

   test("healthy when one node is running") {
      val session = mockk<CqlSession>()
      val metadata = mockk<Metadata>()
      val healthyNode = mockk<Node>()
      every { session.metadata } returns metadata
      every { metadata.nodes } returns mapOf(UUID.randomUUID() to healthyNode)
      every { healthyNode.state } returns NodeState.UP

      val check = CassandraDriverHealthCheck(session)
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("healthy when at least one node is running") {
      val session = mockCqlSessionWithNodeState(
         NodeState.UP, NodeState.UNKNOWN, NodeState.DOWN, NodeState.FORCED_DOWN
      )
      val check = CassandraDriverHealthCheck(session)
      check.check().status shouldBe HealthStatus.Healthy
   }

   test("unhealthy when all nodes are down") {
      val session = mockCqlSessionWithNodeState(NodeState.DOWN, NodeState.FORCED_DOWN)
      val check = CassandraDriverHealthCheck(session)
      check.check().status shouldBe HealthStatus.Unhealthy
   }
})

fun mockCqlSessionWithNodeState(vararg states: NodeState): CqlSession {
   val session = mockk<CqlSession>()
   val metadata = mockk<Metadata>()
   val nodes = states.map { state ->
      mockk<Node> {
         every { this@mockk.state } returns state
      }
   }

   every { session.metadata } returns metadata
   every { metadata.nodes } returns createNodesWithRandomUUID(nodes)

   return session
}

fun createNodesWithRandomUUID(nodes: List<Node>): Map<UUID, Node> =
   nodes.associateBy { UUID.randomUUID() }
