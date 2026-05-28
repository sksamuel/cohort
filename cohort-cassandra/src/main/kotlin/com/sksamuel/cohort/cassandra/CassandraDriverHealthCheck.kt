package com.sksamuel.cohort.cassandra

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.metadata.NodeState

class CassandraDriverHealthCheck(
   private val session: CqlSession,
   override val name: String = "cassandra",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult =
      runCatching { session.metadata.nodes.values }.fold(
         onSuccess = { nodes ->
            // Distinguish three cases:
            //  - no metadata yet (startup race / metadata reset) — the driver hasn't received
            //    the topology yet; reporting "could not access" misleads operators into
            //    debugging connectivity. Surface a distinct message.
            //  - no UP nodes (real outage)
            //  - at least one UP node (healthy)
            when {
               nodes.isEmpty() -> HealthCheckResult.unhealthy("Cassandra node metadata not yet available")
               nodes.any { it.state == NodeState.UP } -> HealthCheckResult.healthy("Cassandra access successful")
               else -> HealthCheckResult.unhealthy("No Cassandra nodes are UP")
            }
         },
         onFailure = { error ->
            HealthCheckResult.unhealthy("Could not access Cassandra", error)
         }
      )
}
