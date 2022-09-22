package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.WarmupHealthCheck
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [HealthCheck] that warms a [DataSource].
 *
 * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
 * returned is open and usable.
 */
class DataSourceWarmup(
   private val ds: DataSource,
   private val timeout: Duration = 1.seconds,
   override val iterations: Int = 5000,
   override val interval: Duration = 2.milliseconds,
) : WarmupHealthCheck() {

   override val name: String = "datasource_warmup"

   override suspend fun warmup() {
      ds.connection.use { it.isValid(timeout.inWholeSeconds.toInt()) }
   }
}
