package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.HeadBucketRequest
import com.amazonaws.services.s3.model.HeadBucketResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity and read permissions to an S3 bucket.
 */
class S3ReadBucketHealthCheck(
   private val bucketName: String,
   private val client: AmazonS3,
   override val name: String = "aws_s3_bucket",
) : HealthCheck {

   constructor(
      bucketName: String,
      createClient: () -> AmazonS3 = { AmazonS3Client.builder().build() },
      name: String = "aws_s3_bucket",
   ) : this(bucketName, createClient(), name)

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            client.headBucket(HeadBucketRequest(bucketName))
         }
      }.fold(
         { HealthCheckResult.healthy("Connected to bucket $bucketName") },
         { HealthCheckResult.unhealthy("Could not connect to bucket $bucketName", it) }
      )
   }
}
