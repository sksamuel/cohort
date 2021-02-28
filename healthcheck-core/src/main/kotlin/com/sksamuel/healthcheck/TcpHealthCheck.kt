package com.sksamuel.healthcheck

import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

@ExperimentalTime
class TcpHealthCheck(
  private val host: String,
  private val port: Int,
  private val connectionTimeout: Duration = 4.seconds
) : HealthCheck {

  override fun check(): HealthCheckResult {
    val socket = Socket()
    val time = measureTime {
      socket.connect(InetSocketAddress(host, port), connectionTimeout.toLongMilliseconds().toInt())
    }
    return if (socket.isConnected) {
      socket.close()
      HealthCheckResult.Healthy("Connected to $host:$port after ${time.toLongMilliseconds()}ms")
    } else {
      HealthCheckResult.Unhealthy("Connection to $host:$port timed out after $connectionTimeout", null)
    }
  }
}
