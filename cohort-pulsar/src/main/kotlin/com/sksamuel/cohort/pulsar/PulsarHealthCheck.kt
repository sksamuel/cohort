package com.sksamuel.cohort.pulsar

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.CancellationException
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
      return try {
         runInterruptible(Dispatchers.IO) {
            client.clusters().getClusters()
         }
         HealthCheckResult.healthy("Connected to Pulsar")
      } catch (c: CancellationException) {
         throw c
      } catch (t: Throwable) {
         HealthCheckResult.unhealthy("Could not connect to Pulsar", t)
      }
   }
}
