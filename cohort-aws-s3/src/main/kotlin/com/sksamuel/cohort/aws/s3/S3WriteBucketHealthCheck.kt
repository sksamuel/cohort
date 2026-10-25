package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlin.random.Random

/**
 * A Cohort [HealthCheck] that checks for connectivity and write permissions to an S3 bucket.
 */
class S3WriteBucketHealthCheck(
   private val bucketName: String,
   val createClient: () -> AmazonS3 = { AmazonS3Client.builder().build() },
   override val name: String = "aws_s3_bucket_write"
) : HealthCheck {

   private suspend fun use(client: AmazonS3): Result<Unit> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            val key = "cohort_" + Random.nextInt(0, Integer.MAX_VALUE)
            try {
               client.putObject(bucketName, key, "test")
            } finally {
               // Best-effort cleanup. The check's documented purpose is to verify write
               // access (s3:PutObject); a missing s3:DeleteObject permission or a transient
               // cleanup failure should not mask a successful put as unhealthy. Swallow
               // any delete error here.
               runCatching { client.deleteObject(bucketName, key) }
            }
         }.map { }
      }.also { client.shutdown() }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.healthy("Put operation to bucket $bucketName successful") },
            { HealthCheckResult.unhealthy("Could not write to bucket $bucketName", it) }
         )
   }
}
