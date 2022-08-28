package com.sksamuel.cohort

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TcpHealthCheck(
  private val host: String,
  private val port: Int,
  private val connectionTimeout: Duration = 4.seconds
) : HealthCheck {

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      val socket = Socket()
      val start = System.currentTimeMillis()
      socket.connect(InetSocketAddress(host, port), connectionTimeout.inWholeMilliseconds.toInt())
      val time = (System.currentTimeMillis() - start).milliseconds
      if (socket.isConnected) {
        withContext(Dispatchers.IO) {
          socket.close()
        }
        HealthCheckResult.Healthy("Connected to $host:$port after ${time.inWholeMilliseconds}ms")
      } else {
        HealthCheckResult.Unhealthy("Connection to $host:$port timed out after $connectionTimeout", null)
      }
    }.getOrElse { HealthCheckResult.Unhealthy("Connection to $host:$port failed", it) }
  }
}
