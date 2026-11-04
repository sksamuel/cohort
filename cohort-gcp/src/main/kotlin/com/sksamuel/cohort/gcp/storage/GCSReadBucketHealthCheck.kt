package com.sksamuel.cohort.gcp.storage

import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity and read permissions to a Google Cloud Storage bucket.
 */
class GCSReadBucketHealthCheck(
   private val bucketName: String,
   val createClient: () -> Storage = { StorageOptions.getDefaultInstance().service },
   override val name: String = "gcs_bucket",
) : HealthCheck {

   private suspend fun use(storage: Storage): Result<Bucket?> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            // returns null if the bucket does not exist; throws on permission/connectivity errors
            storage.get(bucketName)
         }
      }.also { runCatching { storage.close() } }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { bucket ->
               if (bucket == null) HealthCheckResult.unhealthy("Bucket $bucketName does not exist")
               else HealthCheckResult.healthy("Connected to bucket $bucketName")
            },
            { HealthCheckResult.unhealthy("Could not connect to bucket $bucketName", it) }
         )
   }
}
