package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class S3ReadBucketHealthCheckTest : FunSpec({

   test("should be healthy if headBucket succeeds") {
      val client = mockk<AmazonS3>()
      every { client.headBucket(any()) } returns null
      val check = S3ReadBucketHealthCheck("mybucket", client)
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 0) { client.shutdown() }
   }

   test("should be unhealthy if headBucket fails") {
      val client = mockk<AmazonS3>()
      every { client.headBucket(any()) } throws RuntimeException("foo")
      val check = S3ReadBucketHealthCheck("mybucket", client)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 0) { client.shutdown() }
   }
})
