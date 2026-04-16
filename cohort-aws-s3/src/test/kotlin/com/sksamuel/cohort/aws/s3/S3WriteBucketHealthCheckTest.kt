package com.sksamuel.cohort.aws.s3

import com.amazonaws.services.s3.AmazonS3
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class S3WriteBucketHealthCheckTest : FunSpec({

   test("should be healthy if write succeeds") {
      val client = mockk<AmazonS3>()
      every { client.putObject(any(), any(), any<String>()) } returns null
      every { client.deleteObject(any(), any()) } returns Unit
      val check = S3WriteBucketHealthCheck("mybucket", client)
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 1) { client.putObject("mybucket", any(), "test") }
      verify(exactly = 1) { client.deleteObject("mybucket", any()) }
      verify(exactly = 0) { client.shutdown() }
   }

   test("should be unhealthy if put fails") {
      val client = mockk<AmazonS3>()
      every { client.putObject(any(), any(), any<String>()) } throws RuntimeException("foo")
      every { client.deleteObject(any(), any()) } returns Unit
      val check = S3WriteBucketHealthCheck("mybucket", client)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 1) { client.putObject("mybucket", any(), "test") }
      verify(exactly = 1) { client.deleteObject("mybucket", any()) }
      verify(exactly = 0) { client.shutdown() }
   }
})
