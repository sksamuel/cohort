package com.sksamuel.cohort.pulsar

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
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
         client.bookies()
         HealthCheckResult.healthy("Connected to Pulsar")
      }.getOrElse { HealthCheckResult.unhealthy("Could not connect to Pulsar", it) }
   }
}
