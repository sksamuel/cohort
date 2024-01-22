package com.sksamuel.cohort.cpu

import com.sksamuel.cohort.Warmup
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class CryptoWarmup(
   private val algos: Set<String> = setOf("HmacSHA256", "HmacSHA384", "HmacSHA512"),
) : Warmup {

   override val name: String = "crypto_warmup"

   override suspend fun warm(iteration: Int) {
      algos.forEach { algorithm ->
         val secret = Random.nextBytes(4096)
         val str1 = randomAZ(4096 * 12)
         val str2 = randomAZ(4096 * 14)
         val str3 = randomAZ(4096 * 16)
         val mac = Mac.getInstance(algorithm)
         mac.init(SecretKeySpec(secret, algorithm))
         mac.update(str1.encodeToByteArray())
         mac.update(str2.encodeToByteArray())
         mac.doFinal(str3.encodeToByteArray())
      }
   }

   private fun randomAZ(size: Int): String {
      return List(size) { Random.nextInt(65, 90).toChar() }.toCharArray().concatToString()
   }
}
