package com.sksamuel.cohort.mongo

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class GenericMongoConnectionHealthCheck(
   override val name: String = "mongo_connection",
   private val listDatabaseNames: suspend () -> List<String>,
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      withTimeout(5.seconds) {
         val dbs = listDatabaseNames()
         HealthCheckResult.healthy("Connected to mongo instance (${dbs.size} databases)")
      }
    }.getOrElse {
      HealthCheckResult.unhealthy("Could not connect to mongo instance", it)
    }
  }
}
