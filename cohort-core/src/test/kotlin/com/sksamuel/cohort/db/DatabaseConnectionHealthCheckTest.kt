package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthStatus
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DatabaseConnectionHealthCheckTest : FunSpec({

   test("should return healthy if query null and connection is open") {
      DatabaseConnectionHealthCheck(
         createHikariDS(),
      ).check().status shouldBe HealthStatus.Healthy
   }

   test("should return healthy for valid query") {
      val ds = createHikariDS()
      ds.connection.use { it.createStatement().executeUpdate("CREATE TABLE foo2 (id int)") }
      DatabaseConnectionHealthCheck(
         ds,
         query = "select * from foo2"
      ).check().status shouldBe HealthStatus.Healthy
   }

   test("should return unhealthy for invalid query") {
      DatabaseConnectionHealthCheck(
         createHikariDS(),
         query = "selectfff"
      ).check().status shouldBe HealthStatus.Unhealthy
   }

})

fun createHikariDS(): HikariDataSource =
   HikariConfig().apply {
      jdbcUrl = "jdbc:h2:mem:kjs;DB_CLOSE_DELAY=-1"
      username = "sa"
      password = ""
      maximumPoolSize = 1
   }.let { HikariDataSource(it) }
