package com.sksamuel.cohort.azure.blob

import com.azure.storage.blob.BlobServiceClient
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity and read permissions to an Azure Blob Storage container.
 *
 * The [BlobServiceClient] requires a storage account endpoint, so it must be supplied by the caller, for example:
 * `{ BlobServiceClientBuilder().connectionString(connectionString).buildClient() }`.
 */
class AzureBlobReadContainerHealthCheck(
   private val containerName: String,
   private val createClient: () -> BlobServiceClient,
   override val name: String = "azure_blob_container",
) : HealthCheck {

   private suspend fun use(client: BlobServiceClient): Result<Boolean> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            client.getBlobContainerClient(containerName).exists()
         }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { exists ->
               if (exists) HealthCheckResult.healthy("Connected to container $containerName")
               else HealthCheckResult.unhealthy("Container $containerName does not exist")
            },
            { HealthCheckResult.unhealthy("Could not connect to container $containerName", it) }
         )
   }
}
