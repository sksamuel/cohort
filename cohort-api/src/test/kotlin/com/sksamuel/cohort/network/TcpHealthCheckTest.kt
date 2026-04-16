package com.sksamuel.cohort.network

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TcpHealthCheckTest : FunSpec({

   test("returns healthy when server is listening") {
      ServerSocket(0).use { server ->
         TcpHealthCheck("localhost", server.localPort).check().status shouldBe HealthStatus.Healthy
      }
   }

   test("returns unhealthy when nothing is listening on the port") {
      val port = ServerSocket(0).use { it.localPort }
      TcpHealthCheck("localhost", port).check().status shouldBe HealthStatus.Unhealthy
   }

   test("socket is closed after a successful connection") {
      val accepted = CountDownLatch(1)
      var serverSideSocket: java.net.Socket? = null

      val server = ServerSocket(0)
      Thread {
         runCatching {
            serverSideSocket = server.accept()
            accepted.countDown()
         }
      }.also { it.isDaemon = true }.start()

      TcpHealthCheck("localhost", server.localPort).check()

      accepted.await(5, TimeUnit.SECONDS)
      // EOF (-1) means the client closed the connection
      serverSideSocket!!.getInputStream().read() shouldBe -1
      server.close()
   }

   test("socket is closed after a failed connection attempt") {
      // Exercise the exception path — should not leak the un-connected socket
      val port = ServerSocket(0).use { it.localPort }
      repeat(20) {
         TcpHealthCheck("localhost", port).check().status shouldBe HealthStatus.Unhealthy
      }
      // If sockets leaked this would exhaust file descriptors; reaching here means they didn't
   }
})
