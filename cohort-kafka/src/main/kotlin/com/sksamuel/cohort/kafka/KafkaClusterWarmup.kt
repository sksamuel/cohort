package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.Warmup
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.AdminClient

/**
 * A [HealthCheck] that checks that a connection can be made to a kafka cluster, the controller
 * can be located, and at least one node is present.
 */
class KafkaClusterWarmup(
   private val adminClient: AdminClient,
) : Warmup {

   override val name: String = "kafka_warmup"

   override suspend fun warm(iteration: Int) {
      adminClient.describeCluster().controller().toCompletionStage().await()
   }
}
