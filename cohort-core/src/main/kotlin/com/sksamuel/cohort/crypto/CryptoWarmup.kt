package com.sksamuel.cohort.crypto

import com.sksamuel.cohort.WarmupHealthCheck
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * A Cohort [WarmupHealthCheck] that encrypts random strings using the supplied algorithms.
 */
@Deprecated("Use com.sksamuel.cohort.cpu.CryptoWarmup")
class CryptoWarmup(
   private val algos: Set<String> = setOf("HmacSHA256", "HmacSHA384", "HmacSHA512"),
   override val iterations: Int = 1000,
) : WarmupHealthCheck() {

   private fun randomAZ(size: Int): String {
      return List(size) { Random.nextInt(65, 90).toChar() }.toCharArray().concatToString()
   }

   override suspend fun warm(iteration: Int) {
      algos.forEach { algorithm ->
         val secret = Random.nextBytes(4096)
         val str1 = randomAZ(4096 * 12)
         val str2 = randomAZ(4096 * 12)
         val str3 = randomAZ(4096 * 12)
         val mac = Mac.getInstance(algorithm)
         mac.init(SecretKeySpec(secret, algorithm))
         mac.update(str1.encodeToByteArray())
         mac.update(str2.encodeToByteArray())
         mac.doFinal(str3.encodeToByteArray())
      }
   }
}

suspend fun main() {
   val w = CryptoWarmup()
   repeat(1000) {
      w.warm(it)
      println(it)
   }
}
