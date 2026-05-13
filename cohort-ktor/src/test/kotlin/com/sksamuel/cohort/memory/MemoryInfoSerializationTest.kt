package com.sksamuel.cohort.memory

import com.sksamuel.cohort.endpoints.toJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain

class MemoryInfoSerializationTest : FunSpec({

   test("MemoryInfo from live MXBeans serializes to JSON without reflection errors (issue #50)") {
      val info = getMemoryInfo().getOrThrow()
      val json = info.toJson()
      json shouldContain "memoryPools"
      json shouldContain "bufferPools"
   }
})
