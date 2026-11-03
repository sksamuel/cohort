package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.HealthStatus
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HikariConnectionsHealthCheckTest : FunSpec({

   test("registry runs the hikari connection check and terminates cleanly") {
      val dataSource = createHikariDS()
      val registry = createHealthChecks(dataSource)
      try {
         // Force at least one round of checks via an eventually-style assertion, instead of
         // a 5s Thread.sleep with no assertions.
         eventually(5.seconds) {
            val status = registry.status()
            status.healthchecks.values.firstOrNull()?.result?.status shouldBe HealthStatus.Healthy
         }
      } finally {
         registry.close()
         dataSource.close()
      }
   }
})

// Top-level helpers — also used by HikariPendingThreadsHealthCheckTest.
fun createHikariDS(): HikariDataSource =
   HikariConfig().apply {
      // Unique DB name per call so DB_CLOSE_DELAY=-1 doesn't share state across specs/tests.
      jdbcUrl = "jdbc:h2:mem:${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
      username = "sa"
      password = ""
      maximumPoolSize = 1
   }.let { HikariDataSource(it) }

fun createHealthChecks(ds: HikariDataSource): HealthCheckRegistry =
   HealthCheckRegistry(Dispatchers.Default) {
      register(HikariConnectionsHealthCheck(ds, 1), 100.milliseconds, 100.milliseconds)
   }
