package com.sksamuel.cohort.healthcheck.http

import com.sksamuel.cohort.Warmup
import com.sksamuel.cohort.WarmupHealthCheck
import com.sksamuel.cohort.WarmupRegistry
import io.ktor.client.HttpClient
import org.slf4j.LoggerFactory
import kotlin.time.Duration

/**
 * A Cohort [HttpClientWarmup] that executes requests against a supplied Ktor [client].
 */
@Deprecated("Use HttpRequestWarmup")
class HttpClientWarmup(
   private val client: HttpClient,
   override val iterations: Int,
   private val eval: suspend (HttpClient) -> Unit
) : WarmupHealthCheck() {

   override val name = "http_client_warmup"

   override suspend fun warm(iteration: Int) {
      eval(client)
   }
}

/**
 * A Cohort [Warmup] that executes requests to a given endpoint using a supplied Ktor [client].
 *
 * @param delay an optional delay between iterations
 */
class HttpWarmup(
   private val client: HttpClient,
   private val delay: Duration?, // delay between iterations
   private val eval: suspend (HttpClient) -> Unit,
) : Warmup {

   private val logger = LoggerFactory.getLogger(WarmupRegistry::class.java)

   constructor(client: HttpClient, eval: suspend (HttpClient) -> Unit) : this(client, null, eval)

   override suspend fun warm(iteration: Int) {
      eval(client)
   }
}
