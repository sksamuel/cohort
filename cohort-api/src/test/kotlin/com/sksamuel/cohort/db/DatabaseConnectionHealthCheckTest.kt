package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthStatus
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.sql.Connection
import javax.sql.DataSource

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

   test("should return unhealthy for invalid connection") {
      DatabaseConnectionHealthCheck(
         createHikariDS().also { it.isBroken = true },
      ).check().status shouldBe HealthStatus.Unhealthy
   }

})

internal fun createHikariDS() =
   HikariConfig()
      .apply {
         jdbcUrl = "jdbc:h2:mem:kjs;DB_CLOSE_DELAY=-1"
         username = "sa"
         password = ""
         maximumPoolSize = 1
      }
      .let(::HikariDataSource)
      .let(::ProxyDataSource)

internal class ProxyDataSource(
   private val delegate: DataSource,
   var isBroken: Boolean = false,
) : DataSource by delegate {
   override fun getConnection() = ConnectionProxy(delegate.connection, isBroken)
}

internal class ConnectionProxy(
   private val delegate: Connection,
   private val isBroken: Boolean,
) : Connection by delegate {
   override fun isValid(timeout: Int) = if (isBroken) false else delegate.isValid(timeout)
}
