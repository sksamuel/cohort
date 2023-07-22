package com.sksamuel.cohort.db

import com.sksamuel.cohort.Warmup
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [Warmup] that warms a [DataSource] by executing a query.
 *
 * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
 * returned is open and usable.
 */
class DataSourceConnectionWarmup(
   private val ds: DataSource,
   private val query: String,
   private val timeout: Duration = 1.seconds,
) : Warmup {

   override val name: String = "datasource_connection_warmup"

   override suspend fun warm(iteration: Int) {
      ds.connection.use { conn ->
         conn.isValid(timeout.inWholeSeconds.toInt())
         conn.createStatement().use { it.execute(query) }
      }
   }
}
