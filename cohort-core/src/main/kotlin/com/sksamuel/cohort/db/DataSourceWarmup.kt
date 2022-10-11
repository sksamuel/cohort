package com.sksamuel.cohort.db

import com.sksamuel.cohort.Warmup
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [Warmup] that warms a [DataSource].
 *
 * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
 * returned is open and usable.
 */
class DataSourceWarmup(
   private val ds: DataSource,
   private val timeout: Duration = 1.seconds,
) : Warmup {

   override val name: String = "datasource_warmup"

   override suspend fun warm(iteration: Int) {
      ds.connection.use { it.isValid(timeout.inWholeSeconds.toInt()) }
   }
}
