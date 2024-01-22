package com.sksamuel.cohort.db

import com.sksamuel.cohort.WarmupRegistry
import com.sksamuel.cohort.WarmupState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class DataSourceConnectionWarmupTest : FunSpec({

   test("happy path") {
      val ds = createHikariDS()
      ds.connection.use { it.createStatement().executeUpdate("CREATE TABLE foo1 (id int)") }
      val warmups = WarmupRegistry {
         register(DataSourceConnectionWarmup(ds, "select * from foo1"), 3.seconds)
      }
      warmups.state() shouldBe WarmupState.Running
      delay(5.seconds)
      warmups.state() shouldBe WarmupState.Completed
   }

})
