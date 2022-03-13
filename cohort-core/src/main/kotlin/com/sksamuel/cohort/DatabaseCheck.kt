package com.sksamuel.cohort

import javax.sql.DataSource

/**
 * A [Check] that checks that a connection can be established with
 * a [DataSource] and a basic query executed.
 */
class DatabaseCheck(
  private val ds: DataSource,
  private val query: String = "SELECT 1",
) : Check {
  override suspend fun check(): CheckResult {
    val conn = ds.connection
    conn.createStatement().executeQuery(query)
    conn.close()
    return CheckResult.Healthy("Connected to database successfully")
  }
}
