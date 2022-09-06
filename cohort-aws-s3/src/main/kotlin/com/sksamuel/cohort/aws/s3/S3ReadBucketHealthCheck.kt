package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.HeadBucketRequest
import com.amazonaws.services.s3.model.HeadBucketResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap

/**
 * A Cohort [HealthCheck] that checks for connectivity and read permissions to an S3 bucket.
 */
class S3ReadBucketHealthCheck(
   private val bucketName: String,
   val createClient: () -> AmazonS3
) : HealthCheck {

   override val name: String = "aws_s3_bucket"

   private fun use(client: AmazonS3): Result<HeadBucketResult> {
      return runCatching { client.headBucket(HeadBucketRequest(bucketName)) }.also { client.shutdown() }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.Healthy("Connected to bucket $bucketName") },
            { HealthCheckResult.Unhealthy("Could not connect to bucket $bucketName", it) }
         )
   }
}
