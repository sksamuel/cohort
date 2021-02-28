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
    conn.createStatement().executeQuery("SELECT * FROM $tableName LIMIT 1")
    conn.close()
    return HealthCheckResult.Healthy("Executed query against $tableName successfully")
  }
}
