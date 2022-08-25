package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.HeadBucketRequest
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks for connectivity and read permissions to an S3 bucket.
 */
class S3ReadBucketHealthCheck(
  private val bucketName: String,
  val createClient: () -> AmazonS3
) : HealthCheck {
  override suspend fun check(): HealthCheckResult {
    return runCatching {
      createClient().headBucket(HeadBucketRequest(bucketName))
    }.fold(
      { HealthCheckResult.Healthy("Connected to bucket $bucketName") },
      { HealthCheckResult.Unhealthy("Could not connect to bucket $bucketName", it) }
    )
  }
}
