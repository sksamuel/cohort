package com.sksamuel.cohort.elastic

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.CountRequest
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A cohort [HealthCheck] which checks that an elastic index exists, and optionally, that it is not empty.
 *
 * @param client the high level rest client to use to connect.
 * @param index the index name to check for.
 * @param failIfEmpty if true, the health check will fail if the elastic index is empty.
 */
class ElasticIndexHealthCheck(
   private val client: ElasticsearchClient,
   private val index: String,
   private val failIfEmpty: Boolean = false,
) : HealthCheck {

   override val name: String = "elastic_index"

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         withContext(Dispatchers.IO) {
            val count = client.count(CountRequest.Builder().index(index).build())
            if (count.count() == 0L && failIfEmpty) {
               HealthCheckResult.unhealthy("Elastic index '$index' is empty")
            } else {
               HealthCheckResult.healthy("Detected elastic index '$index'")
            }
         }
      }.getOrElse {
         if (it.message?.contains("index_not_found_exception") == true) {
            HealthCheckResult.unhealthy("Elastic index '$index' was not found", it)
         } else {
            HealthCheckResult.unhealthy("Error connecting to elastic cluster", it)
         }
      }
   }
}
