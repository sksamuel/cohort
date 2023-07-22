package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.HealthCheckRegistry
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlin.time.Duration.Companion.seconds

class HikariConnectionsHealthCheckTest : FunSpec({

   test("HealthCheckRegistry should terminate") {
      val dataSource = createHikariDS()
      val registry = createHealthChecks(dataSource)
      Thread.sleep(5000) // let the healthcheck run a few times
      dataSource.close()
      registry.close()
   }
})

fun createHikariDS(): HikariDataSource =
   HikariConfig().apply {
      jdbcUrl = "jdbc:h2:mem:kjs;DB_CLOSE_DELAY=-1"
      username = "sa"
      password = ""
      maximumPoolSize = 1
   }.let { HikariDataSource(it) }

fun createHealthChecks(ds: HikariDataSource): HealthCheckRegistry =
   HealthCheckRegistry(Dispatchers.Default) {
      register(HikariConnectionsHealthCheck(ds, 1), 1.seconds)
   }
