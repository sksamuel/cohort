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
   private val client: AmazonS3,
   override val name: String = "aws_s3_bucket_write"
) : HealthCheck {

   constructor(
      bucketName: String,
      createClient: () -> AmazonS3 = { AmazonS3Client.builder().build() },
      name: String = "aws_s3_bucket_write"
   ) : this(bucketName, createClient(), name)

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            val key = "cohort_" + Random.nextInt(0, Integer.MAX_VALUE)
            try {
               client.putObject(bucketName, key, "test")
            } finally {
               runCatching { client.deleteObject(bucketName, key) }
            }
         }
      }.fold(
         { HealthCheckResult.healthy("Put operation to bucket $bucketName successful") },
         { HealthCheckResult.unhealthy("Could not write to bucket $bucketName", it) }
      )
   }
}
