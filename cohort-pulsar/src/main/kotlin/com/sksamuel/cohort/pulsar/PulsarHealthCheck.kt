package com.sksamuel.cohort.pulsar

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.apache.pulsar.client.admin.PulsarAdmin

class PulsarHealthCheck(
   private val client: PulsarAdmin,
   override val name: String = "pulsar_cluster",
) : HealthCheck {

   companion object {
      operator fun invoke(serviceHttpUrl: String): PulsarHealthCheck {
         return PulsarHealthCheck(
            PulsarAdmin.builder().serviceHttpUrl(serviceHttpUrl).build()
         )
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         val clusters = runInterruptible(Dispatchers.IO) {
            client.clusters().getClusters()
         }
         // An empty cluster list means the broker is reachable but unconfigured (no clusters
         // registered). Without this check, a misconfigured broker silently reports healthy.
         if (clusters.isNullOrEmpty())
            HealthCheckResult.unhealthy("Pulsar reachable but reports no clusters", null)
         else
            HealthCheckResult.healthy("Connected to Pulsar; clusters=${clusters.joinToString(",")}")
      }.getOrElse { HealthCheckResult.unhealthy("Could not connect to Pulsar", it) }
   }
}
