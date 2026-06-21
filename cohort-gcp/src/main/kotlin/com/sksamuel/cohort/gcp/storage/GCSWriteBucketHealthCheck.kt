package com.sksamuel.cohort.gcp.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlin.random.Random

/**
 * A Cohort [HealthCheck] that checks for connectivity and write permissions to a Google Cloud Storage bucket.
 */
class GCSWriteBucketHealthCheck(
   private val bucketName: String,
   val createClient: () -> Storage = { StorageOptions.getDefaultInstance().service },
   override val name: String = "gcs_bucket_write",
) : HealthCheck {

   private suspend fun use(storage: Storage): Result<Unit> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            val key = "cohort_" + Random.nextInt(0, Integer.MAX_VALUE)
            val blobId = BlobId.of(bucketName, key)
            try {
               storage.create(BlobInfo.newBuilder(blobId).build(), "test".toByteArray())
            } finally {
               // Best-effort cleanup. The check's documented purpose is to verify write
               // access (storage.objects.create); a missing delete permission or a transient
               // cleanup failure should not mask a successful write as unhealthy. Swallow
               // any delete error here.
               runCatching { storage.delete(blobId) }
            }
         }.map { }
      }.also { runCatching { storage.close() } }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.healthy("Write operation to bucket $bucketName successful") },
            { HealthCheckResult.unhealthy("Could not write to bucket $bucketName", it) }
         )
   }
}
