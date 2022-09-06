package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a connection can be established with
 * a [DataSource] and a basic query executed.
 *
 * @deprecated use DatabaseConnectionHealthCheck which supports the JDBC4 isValid method.
 */
@Deprecated("Use DatabaseConnectionHealthCheck")
class DatabaseHealthCheck(
   private val ds: DataSource,
   private val query: String = "SELECT 1",
) : HealthCheck {

   override val name: String = "database"

   override suspend fun check(): HealthCheckResult = ds.connection.use { conn ->
      conn.createStatement().executeQuery(query)
      HealthCheckResult.Healthy("Connected to database successfully")
   }
}
