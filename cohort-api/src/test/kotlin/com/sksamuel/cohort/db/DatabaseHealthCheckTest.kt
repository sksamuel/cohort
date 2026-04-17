package com.sksamuel.cohort.db

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("DEPRECATION")
class DatabaseHealthCheckTest : FunSpec({

   test("returns healthy when default SELECT 1 query succeeds") {
      DatabaseHealthCheck(createHikariDS()).check().status shouldBe HealthStatus.Healthy
   }

   test("returns healthy when a custom query succeeds") {
      val ds = createHikariDS()
      ds.connection.use { it.createStatement().executeUpdate("CREATE TABLE test_table (id int)") }
      DatabaseHealthCheck(ds, query = "SELECT * FROM test_table").check().status shouldBe HealthStatus.Healthy
   }
})
