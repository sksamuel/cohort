package com.sksamuel.healthcheck

import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a connection can be established with a database.
 */
class DatabaseHealthCheck(
  private val ds: DataSource,
  private val query: String = "SELECT 1",
) : HealthCheck {
  override fun check(): HealthCheckResult {
    val conn = ds.connection
    conn.createStatement().executeQuery(query)
    conn.close()
    return HealthCheckResult.Healthy("Connected to database successfully")
  }
}
