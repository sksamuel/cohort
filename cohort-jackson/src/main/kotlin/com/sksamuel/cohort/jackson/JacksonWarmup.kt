package com.sksamuel.cohort.jackson

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sksamuel.cohort.cpu.FibWarmup
import com.sksamuel.cohort.WarmupHealthCheck
import com.sksamuel.cohort.cpu.HotSpotCompilationTimeHealthCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.lang.management.ManagementFactory
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A [WarmupHealthCheck] that will marshall and unmarshall JSON.
 */
class JacksonWarmup(
   override val iterations: Int = 5000,
   override val interval: Duration = 2.milliseconds,
) : WarmupHealthCheck() {

   private val mapper = jacksonObjectMapper()
   override val name: String = "jackson_warmup"

   override suspend fun warmup() {
      val fake = mapper.readValue<Fake>(json())
      mapper.writeValueAsBytes(fake)
   }
}

data class Fake(
   val a: String,
   val b: Int,
   val c: Long,
   val d: Boolean,
   val e: Double,
   val f: Float,
   val g: BigDecimal,
   val h: BigInteger,
   val i: Short,
   val j: Byte,
)

private fun json() = """{
   "a": "foo",
   "b": ${Random.nextInt()},
   "c": ${Random.nextLong()},
   "d": ${Random.nextBoolean()},
   "e": ${Random.nextDouble()},
   "f": ${Random.nextFloat()},
   "g": ${Random.nextDouble()},
   "h": ${Random.nextInt()},
   "i": ${Random.nextInt(0, 1023)},
   "j": ${Random.nextInt(0, 255)}
   }"""

suspend fun main() {
   ManagementFactory.getClassLoadingMXBean().isVerbose = true
   val jackson = JacksonWarmup()
   val hotspot = HotSpotCompilationTimeHealthCheck(2000)
   val fib = FibWarmup()
   val scope = CoroutineScope(Dispatchers.IO)
   jackson.start(scope)
   fib.start(scope)
   while (true) {
      delay(500)
      println(ManagementFactory.getCompilationMXBean().totalCompilationTime)
      println(ManagementFactory.getClassLoadingMXBean().loadedClassCount)
      println(jackson.check())
      println(hotspot.check())
      println(fib.check())
   }
}
