package com.sksamuel.cohort.azure.blob

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobServiceClient
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlin.random.Random

/**
 * A Cohort [HealthCheck] that checks for connectivity and write permissions to an Azure Blob Storage container.
 *
 * The [BlobServiceClient] requires a storage account endpoint, so it must be supplied by the caller, for example:
 * `{ BlobServiceClientBuilder().connectionString(connectionString).buildClient() }`.
 */
class AzureBlobWriteContainerHealthCheck(
   private val containerName: String,
   private val createClient: () -> BlobServiceClient,
   override val name: String = "azure_blob_container_write",
) : HealthCheck {

   private suspend fun use(client: BlobServiceClient): Result<Unit> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            val key = "cohort_" + Random.nextInt(0, Integer.MAX_VALUE)
            val blob = client.getBlobContainerClient(containerName).getBlobClient(key)
            try {
               blob.upload(BinaryData.fromString("test"), true)
            } finally {
               // Best-effort cleanup. The check's documented purpose is to verify write
               // access; a missing delete permission or a transient cleanup failure should
               // not mask a successful upload as unhealthy. Swallow any delete error here.
               runCatching { blob.deleteIfExists() }
            }
         }.map { }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.healthy("Write operation to container $containerName successful") },
            { HealthCheckResult.unhealthy("Could not write to container $containerName", it) }
         )
   }
}
