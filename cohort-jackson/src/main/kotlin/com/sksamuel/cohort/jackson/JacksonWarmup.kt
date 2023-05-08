package com.sksamuel.cohort.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sksamuel.cohort.WarmupHealthCheck
import com.sksamuel.cohort.crypto.CryptoWarmup
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

/**
 * A [WarmupHealthCheck] that will marshall and unmarshall JSON.
 */
class JacksonWarmup(
   private val mapper: ObjectMapper = jacksonObjectMapper(),
   override val iterations: Int = 1000,
) : WarmupHealthCheck() {

   override val name: String = "jackson_warmup"

   private fun randomAZ(size: Int) = List(size) { Random.nextInt(65, 90).toChar() }.toCharArray().concatToString()

   override suspend fun warm(iteration: Int) {
      val fake = Fake(
         a = randomAZ(1024),
         b = Random.nextInt(),
         c = Random.nextLong(),
         d = Random.nextBoolean(),
         e = Random.nextDouble(),
         f = Random.nextFloat(),
         g = BigDecimal.valueOf(Random.nextDouble()),
         h = BigInteger.valueOf(Random.nextLong()),
         i = Random.nextBytes(1).first().toShort(),
         j = Random.nextBytes(1).first(),
         k = List(100) { randomAZ(128) },
         l = List(100) { randomAZ(128) }.toSet(),
      )
      val json = mapper.writeValueAsString(fake)
      mapper.readValue<Fake>(json)
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
   val k: List<String>,
   val l: Set<String>,
)

suspend fun main() {
   val w = CryptoWarmup()
   repeat(1000) {
      w.warm(it)
      println(it)
   }
}
