package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.CheckResult
import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a query can be executed against a table in a database.
 */
class DatabaseTableHealthCheck(
  private val ds: DataSource,
  private val tableName: String,
) : HealthCheck {
  override suspend fun check(): CheckResult {
    val conn = ds.connection
    conn.createStatement().executeQuery("SELECT * FROM $tableName LIMIT 1")
    conn.close()
    return CheckResult.Healthy("Executed query against $tableName successfully")
  }
}
