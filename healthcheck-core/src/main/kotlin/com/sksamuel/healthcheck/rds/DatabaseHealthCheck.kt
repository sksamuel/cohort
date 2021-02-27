package com.sksamuel.healthcheck.rds

import com.sksamuel.healthcheck.HealthCheck
import com.sksamuel.healthcheck.HealthCheckResult
import javax.sql.DataSource

/**
 * A [HealthCheck] that checks that a connection can be made to a database and a
 * basic select can be issued against a table.
 */
class DatabaseHealthCheck(
  private val ds: DataSource,
  private val tableName: String,
) : HealthCheck {
  override fun check(): HealthCheckResult {
    val conn = ds.connection
    return try {
      conn.createStatement().executeQuery("SELECT * FROM $tableName WHERE 1=0")
      HealthCheckResult.Healthy
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Error connecting to database", t)
    } finally {
      conn.close()
    }
  }
}
