package com.sksamuel.cohort.redis

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import redis.clients.jedis.JedisCluster

class RedisClusterHealthCheckTest : FunSpec({

   test("should be healthy if nodes > 0") {
      val jedis = mockk<JedisCluster>()
      every { jedis.clusterNodes } returns mapOf("foo" to mockk())
      val check = RedisClusterHealthCheck(jedis)
      check.check().status shouldBe HealthStatus.Healthy
      check.check().status shouldBe HealthStatus.Healthy
      verify(exactly = 0) { jedis.close() }
   }

   test("should be unhealthy if nodes == 0") {
      val jedis = mockk<JedisCluster>()
      every { jedis.clusterNodes } returns emptyMap()
      val check = RedisClusterHealthCheck(jedis)
      check.check().status shouldBe HealthStatus.Unhealthy
      verify(exactly = 0) { jedis.close() }
   }
})
