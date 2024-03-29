package com.sksamuel.cohort.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sksamuel.cohort.Warmup
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.random.Random

/**
 * A [Warmup] that will marshall and unmarshall JSON using Jackson.
 */
class JacksonMapperWarmup(
   private val mapper: ObjectMapper = jacksonObjectMapper(),
) : Warmup {

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
      val obj = mapper.readValue<Fake>(json)
      if (Random.nextInt(1, 2) == 0) println(obj)
   }
}
