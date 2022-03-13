package com.sksamuel.cohort

import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.seconds

@ExperimentalTime
class TcpCheck(
  private val host: String,
  private val port: Int,
  private val connectionTimeout: Duration = 4.seconds
) : Check {

  override fun check(): CheckResult {
    val socket = Socket()
    val time = measureTime {
      socket.connect(InetSocketAddress(host, port), connectionTimeout.toLongMilliseconds().toInt())
    }
    return if (socket.isConnected) {
      socket.close()
      CheckResult.Healthy("Connected to $host:$port after ${time.toLongMilliseconds()}ms")
    } else {
      CheckResult.Unhealthy("Connection to $host:$port timed out after $connectionTimeout", null)
    }
  }
}
