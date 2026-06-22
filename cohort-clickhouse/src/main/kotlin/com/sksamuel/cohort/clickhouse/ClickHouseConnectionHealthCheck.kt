package com.sksamuel.cohort.clickhouse

import com.clickhouse.client.api.Client
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity to a ClickHouse database.
 *
 * Uses the ClickHouse client's ping operation to confirm the server is reachable and responding.
 *
 * The [Client] is owned by the caller and is not closed by this check.
 */
class ClickHouseConnectionHealthCheck(
   private val client: Client,
   override val name: String = "clickhouse",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult = runCatching {
      runInterruptible(Dispatchers.IO) {
         client.ping()
      }
   }.fold(
      { alive ->
         if (alive) HealthCheckResult.healthy("Connected to ClickHouse successfully")
         else HealthCheckResult.unhealthy("Could not connect to ClickHouse")
      },
      { HealthCheckResult.unhealthy("Could not connect to ClickHouse", it) }
   )
}
