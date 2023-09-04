package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.Admin

/**
 * A [HealthCheck] that checks that a connection can be made to a kafka cluster, the controller
 * can be located, and at least one node is present.
 */
class KafkaClusterHealthCheck(private val admin: Admin) : HealthCheck {

   override val name: String = "kafka_cluster"

   override suspend fun check(): HealthCheckResult {
      return try {
         val clusterResult = admin.describeCluster()
         val controller = clusterResult.controller().toCompletionStage().await()
         val nodes = clusterResult.nodes().toCompletionStage().await()

         when {
            nodes.isEmpty() -> HealthCheckResult.unhealthy("Kafka cluster is showing no nodes", null)
            controller == null -> HealthCheckResult.unhealthy("Kafka cluster returned without controller", null)
            else -> HealthCheckResult.healthy("Connected to kafka cluster with controller ${controller.host()} and ${nodes.size} node(s)")
         }
      } catch (t: Throwable) {
         HealthCheckResult.unhealthy("Could not connect to kafka cluster", t)
      }
   }
}
