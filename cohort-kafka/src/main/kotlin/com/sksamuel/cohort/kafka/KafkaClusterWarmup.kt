package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.Warmup
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.admin.AdminClient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [HealthCheck] that checks that a connection can be made to a kafka cluster, the controller
 * can be located, and at least one node is present.
 */
class KafkaClusterWarmup(
   private val adminClient: AdminClient,
   override val iterations: Int = 1000,
   override val interval: Duration = 10.milliseconds
) : Warmup() {

   override val name: String = "kafka_warmup"

   override suspend fun warmup() {
      adminClient.describeCluster().controller().toCompletionStage().await()
   }
}
