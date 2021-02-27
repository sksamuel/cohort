package com.sksamuel.healthcheck

import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class TcpHealthCheck(
  private val host: String,
  private val port: Int,
  private val connectionTimeout: Duration = 4.seconds
) : HealthCheck {
  override fun check(): HealthCheckResult {
    return try {
      val socket = Socket()
      socket.connect(InetSocketAddress(host, port), connectionTimeout.toLongMilliseconds().toInt())
      if (socket.isConnected)
        HealthCheckResult.Healthy else
        HealthCheckResult.Unhealthy("Connection to $host:$port timed out after $connectionTimeout", null)
    } catch (t: Throwable) {
      HealthCheckResult.Unhealthy("Connection to $host:$port failed", t)
    }
  }
}
