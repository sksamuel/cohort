package com.sksamuel.cohort.azure.blob

import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class AzureBlobContainerHealthCheckContainerTest : FunSpec({

   val blobPort = 10000
   // Well-known Azurite default development account and key.
   val accountName = "devstoreaccount1"
   val accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="

   val azurite = GenericContainer(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:latest")).apply {
      withExposedPorts(blobPort)
      // The storage SDK targets a newer REST API version than Azurite recognises, so skip the
      // version check; otherwise requests are rejected with HTTP 400 InvalidHeaderValue.
      withCommand("azurite-blob", "--blobHost", "0.0.0.0", "--skipApiVersionCheck")
      waitingFor(Wait.forLogMessage(".*Blob service successfully listens.*", 1))
   }

   beforeSpec {
      azurite.start()
   }

   afterSpec {
      azurite.close()
   }

   fun createClient(): BlobServiceClient {
      val endpoint = "http://${azurite.host}:${azurite.getMappedPort(blobPort)}/$accountName"
      val connectionString =
         "DefaultEndpointsProtocol=http;AccountName=$accountName;AccountKey=$accountKey;BlobEndpoint=$endpoint;"
      return BlobServiceClientBuilder().connectionString(connectionString).buildClient()
   }

   test("read check is healthy when the container exists") {
      val container = "cohort-read"
      createClient().createBlobContainerIfNotExists(container)
      AzureBlobReadContainerHealthCheck(container, ::createClient).check().status shouldBe HealthStatus.Healthy
   }

   test("read check is unhealthy when the container does not exist") {
      AzureBlobReadContainerHealthCheck("no-such-container", ::createClient).check().status shouldBe HealthStatus.Unhealthy
   }

   test("read check is unhealthy when the server cannot be reached") {
      val unreachable = { BlobServiceClientBuilder().connectionString(
         "DefaultEndpointsProtocol=http;AccountName=$accountName;AccountKey=$accountKey;BlobEndpoint=http://${azurite.host}:1/$accountName;"
      ).buildClient() }
      AzureBlobReadContainerHealthCheck("cohort-read", unreachable).check().status shouldBe HealthStatus.Unhealthy
   }

   test("write check is healthy when the container exists") {
      val container = "cohort-write"
      createClient().createBlobContainerIfNotExists(container)
      AzureBlobWriteContainerHealthCheck(container, ::createClient).check().status shouldBe HealthStatus.Healthy
   }

   test("write check is unhealthy when the container does not exist") {
      AzureBlobWriteContainerHealthCheck("no-such-container", ::createClient).check().status shouldBe HealthStatus.Unhealthy
   }
})
