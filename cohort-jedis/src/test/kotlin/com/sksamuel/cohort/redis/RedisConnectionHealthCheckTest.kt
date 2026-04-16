package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import redis.clients.jedis.Connection
import redis.clients.jedis.Jedis

class RedisConnectionHealthCheckTest : FunSpec({

   fun jedisWithPingResult(result: Boolean): Jedis {
      val conn = mockk<Connection>(relaxed = true)
      every { conn.ping() } returns result
      val jedis = mockk<Jedis>()
      every { jedis.connection } returns conn
      return jedis
   }

   test("returns healthy when ping succeeds") {
      RedisConnectionHealthCheck(jedisWithPingResult(true)).check().status shouldBe HealthStatus.Healthy
   }

   test("returns unhealthy when ping fails") {
      RedisConnectionHealthCheck(jedisWithPingResult(false)).check().status shouldBe HealthStatus.Unhealthy
   }

   test("should not close connection so that subsequent calls work") {
      val conn = mockk<Connection>(relaxed = true)
      every { conn.ping() } returns true
      val jedis = mockk<Jedis>()
      every { jedis.connection } returns conn

      val check = RedisConnectionHealthCheck(jedis)
      check.check().status shouldBe HealthStatus.Healthy
      check.check().status shouldBe HealthStatus.Healthy

      verify(exactly = 0) { conn.close() }
   }
})
