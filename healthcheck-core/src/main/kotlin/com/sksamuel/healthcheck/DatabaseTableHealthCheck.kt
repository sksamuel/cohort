package com.sksamuel.healthcheck

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a query can be executed against a table in a database.
 */
class DatabaseTableHealthCheck(
  private val ds: DataSource,
  private val tableName: String,
) : HealthCheck {
  override suspend fun check(): HealthCheckResult = withContext(Dispatchers.IO) {
    val conn = ds.connection
    conn.createStatement().executeQuery("SELECT * FROM $tableName LIMIT 1")
    conn.close()
    HealthCheckResult.Healthy("Executed query against $tableName successfully")
  }
}
