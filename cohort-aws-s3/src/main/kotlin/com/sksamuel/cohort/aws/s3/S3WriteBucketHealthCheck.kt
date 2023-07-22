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
   val createClient: () -> AmazonS3 = { AmazonS3Client.builder().build() }
) : HealthCheck {

   override val name: String = "aws_s3_bucket_write"

   private suspend fun use(client: AmazonS3): Result<Unit> {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            val key = "cohort_" + Random.nextInt(0, Integer.MAX_VALUE)
            client.putObject(bucketName, key, "test")
            client.deleteObject(bucketName, key)
         }
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
