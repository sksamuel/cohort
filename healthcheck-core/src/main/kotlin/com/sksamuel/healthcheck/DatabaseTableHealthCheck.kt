package com.sksamuel.healthcheck

import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a query can be executed against a table in a database.
 */
class DatabaseTableHealthCheck(
  private val ds: DataSource,
  private val tableName: String,
) : HealthCheck {
  override fun check(): HealthCheckResult {
    val conn = ds.connection
    return try {
      conn.createStatement().executeQuery("SELECT * FROM $tableName LIMIT 1")
      HealthCheckResult.Healthy("Executed query against $tableName successfully")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error executing query against $tableName", t)
    } finally {
      conn.close()
    }
  }
}
