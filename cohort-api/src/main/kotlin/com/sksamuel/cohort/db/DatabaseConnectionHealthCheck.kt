package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [HealthCheck] that checks that a connection can be retrieved from a [DataSource].
 *
 * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
 * returned is open and usable.
 *
 * An optional query can be supplied that will be executed in addition to checking the connection is valid.
 */
class DatabaseConnectionHealthCheck(
   private val ds: DataSource,
   private val timeout: Duration = 1.seconds,
   private val query: String? = null,
   override val name: String = "database_connection",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult = runCatching {
      ds.connection.use { conn ->
         if (!conn.isValid(timeout.inWholeSeconds.toInt())) {
            return@use HealthCheckResult.unhealthy("Connection is invalid")
         }
         if (query != null)
            conn.createStatement().use { it.execute(query) }
         HealthCheckResult.healthy("Connected to database successfully")
      }
   }.getOrElse { HealthCheckResult.unhealthy("Unable to connect to the database", it) }
}
