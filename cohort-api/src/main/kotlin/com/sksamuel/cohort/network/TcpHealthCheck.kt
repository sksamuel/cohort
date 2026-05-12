package com.sksamuel.cohort.network

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TcpHealthCheck(
   private val host: String,
   private val port: Int,
   private val connectionTimeout: Duration = 4.seconds,
   override val name: String = "tcp",
) : HealthCheck {

   override suspend fun check(): HealthCheckResult {
      return runCatching {
         withContext(Dispatchers.IO) {
            Socket().use { socket ->
               val start = System.currentTimeMillis()
               // Socket.connect either returns successfully (isConnected == true) or throws.
               // SocketTimeoutException is the specific failure for connectionTimeout expiring;
               // distinguish it so operators see "timed out" instead of a generic "failed".
               socket.connect(InetSocketAddress(host, port), connectionTimeout.inWholeMilliseconds.toInt())
               val time = (System.currentTimeMillis() - start).milliseconds
               HealthCheckResult.healthy("Connected to $host:$port after ${time.inWholeMilliseconds}ms")
            }
         }
      }.getOrElse { t ->
         if (t is SocketTimeoutException)
            HealthCheckResult.unhealthy("Connection to $host:$port timed out after $connectionTimeout", t)
         else
            HealthCheckResult.unhealthy("Connection to $host:$port failed", t)
      }
   }
}
