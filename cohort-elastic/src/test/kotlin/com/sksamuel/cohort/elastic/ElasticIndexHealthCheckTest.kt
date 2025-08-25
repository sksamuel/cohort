//package com.sksamuel.cohort.elastic
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient
//import co.elastic.clients.elasticsearch._types.Refresh
//import co.elastic.clients.elasticsearch.core.IndexRequest
//import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
//import co.elastic.clients.json.jackson.JacksonJsonpMapper
//import co.elastic.clients.transport.rest_client.RestClientTransport
//import com.sksamuel.cohort.HealthCheckResult
//import com.sksamuel.cohort.HealthStatus
//import io.kotest.core.extensions.install
//import io.kotest.core.spec.style.FunSpec
//import io.kotest.extensions.testcontainers.elastic.ElasticsearchContainerExtension
//import io.kotest.extensions.testcontainers.elastic.client
//import io.kotest.matchers.shouldBe
//import org.apache.http.HttpHost
//import org.elasticsearch.client.RestClient
//import org.testcontainers.utility.DockerImageName
//
//class ElasticIndexHealthCheckTest : FunSpec({
//
//   val container = install(
//      ElasticsearchContainerExtension(
//         DockerImageName.parse("elasticsearch:7.17.6")
//            .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
//      )
//   )
//
//   val client = container.client()
//
//   test("ElasticIndexCheck should connect to elastic and check for topic") {
//      client.indices().create(CreateIndexRequest.Builder().index("foo").build())
//      ElasticIndexHealthCheck(client, "foo").check() shouldBe
//         HealthCheckResult.healthy("Detected elastic index 'foo'")
//   }
//
//   test("missing index") {
//      ElasticIndexHealthCheck(client, "qwe").check()
//         .message shouldBe "Elastic index 'qwe' was not found"
//   }
//
//   test("ElasticIndexCheck should support failIfEmpty=true with empty index") {
//      client.indices().create(CreateIndexRequest.Builder().index("bar").build())
//      ElasticIndexHealthCheck(client, "bar", true).check()
//         .message shouldBe "Elastic index 'bar' is empty"
//   }
//
//   test("ElasticIndexCheck should support failIfEmpty=true with populated index") {
//      client.indices().create(CreateIndexRequest.Builder().index("baz").build())
//      client.index(
//         IndexRequest.Builder<Map<String, String>>().index("baz").document(mapOf("a" to "b"))
//            .refresh(Refresh.True).build()
//      )
//      ElasticIndexHealthCheck(client, "baz", true).check() shouldBe
//         HealthCheckResult.healthy("Detected elastic index 'baz'")
//   }
//
//   test("ElasticIndexHealthCheck should fail if cannot connect") {
//      val restClient = RestClient.builder(HttpHost("localhost", 11111)).build()
//      val transport = RestClientTransport(restClient, JacksonJsonpMapper())
//      val client2 = ElasticsearchClient(transport)
//      ElasticIndexHealthCheck(client2, "foo").check().status shouldBe HealthStatus.Unhealthy
//   }
//})
