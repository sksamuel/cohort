package com.sksamuel.cohort.crypto

import com.sksamuel.cohort.Warmup
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class CryptoWarmup : Warmup {

   private val algos = setOf("HmacSHA256", "HmacSHA384", "HmacSHA512")

   private fun randomAZ(size: Int): String {
      return List(size) { Random.nextInt(65, 90).toChar() }.toCharArray().concatToString()
   }

   override suspend fun warm(iteration: Int) {
      algos.forEach { algorithm ->
         val secret = Random.nextBytes(32)
         val str1 = randomAZ(4096)
         val str2 = randomAZ(4096)
         val str3 = randomAZ(4096)
         val mac = Mac.getInstance(algorithm)
         mac.init(SecretKeySpec(secret, algorithm))
         mac.update(str1.encodeToByteArray())
         mac.update(str2.encodeToByteArray())
         val bytes = mac.doFinal(str3.encodeToByteArray())
      }
   }
}

suspend fun main() {
   val w = CryptoWarmup()
   repeat(1000) {
      w.warm(it)
   }
}
