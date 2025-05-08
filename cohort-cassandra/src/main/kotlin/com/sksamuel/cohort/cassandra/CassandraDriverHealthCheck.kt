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
      runCatching {
         session.metadata.nodes.values.any { node -> node.state == NodeState.UP }
      }.fold(
         onSuccess = { anyUp ->
            if (anyUp) HealthCheckResult.healthy("Cassandra access successful")
            else HealthCheckResult.unhealthy("Could not access to Cassandra")
         },
         onFailure = { error ->
            HealthCheckResult.unhealthy("Could not access to Cassandra", error)
         }
      )
}
