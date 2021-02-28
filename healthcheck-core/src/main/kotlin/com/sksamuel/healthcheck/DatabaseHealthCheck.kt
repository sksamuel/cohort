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
    return try {
      conn.createStatement().executeQuery(query)
      HealthCheckResult.Healthy("Connected to database successfully")
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error connecting to database", t)
    } finally {
      conn.close()
    }
  }
}
