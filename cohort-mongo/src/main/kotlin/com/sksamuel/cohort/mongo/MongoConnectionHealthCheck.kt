package com.sksamuel.cohort.mongo

import com.mongodb.client.MongoClient
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class MongoConnectionHealthCheck(private val client: MongoClient) : HealthCheck {

  override val name: String = "mongo_connection"

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      withTimeout(5.seconds) {
        runInterruptible(Dispatchers.IO) {
          val dbs = client.listDatabaseNames().toList()
          HealthCheckResult.healthy("Connected to mongo instance (${dbs.size} databases)")
        }
      }
    }.getOrElse {
      HealthCheckResult.unhealthy("Could not connect to mongo instance", it)
    }
  }
}
