package com.sksamuel.cohort.vertx

import com.sksamuel.cohort.endpoints.CohortConfiguration
import com.sksamuel.cohort.gc.getGcInfo
import com.sksamuel.cohort.heap.getHeapDump
import com.sksamuel.cohort.jvm.getJvmDetails
import com.sksamuel.cohort.logging.LogInfo
import com.sksamuel.cohort.memory.getMemoryInfo
import com.sksamuel.cohort.os.getOperatingSystem
import com.sksamuel.cohort.system.getSysProps
import com.sksamuel.cohort.threads.getThreadDump
import com.sksamuel.tabby.results.sequence
import io.netty.handler.codec.compression.StandardCompressionOptions
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.time.ZoneOffset

/**
 * Creates a [CoroutineVerticle] that will deploy an HTTP server on the given [port].
 */
class HealthVerticle(
   private val port: Int,
   private val options: HttpServerOptions,
   configure: CohortConfiguration.() -> Unit = {},
) : CoroutineVerticle() {

   companion object {

      operator fun invoke(
         port: Int,
         configure: CohortConfiguration.() -> Unit = {},
      ): HealthVerticle {
         val options = HttpServerOptions()
            .setCompressionSupported(true)
            .setDecompressionSupported(true)
            .addCompressor(StandardCompressionOptions.gzip())
         return HealthVerticle(port, options, configure)
      }
   }

   private val cohort = CohortConfiguration().also(configure)
   private val logger = LoggerFactory.getLogger(HealthVerticle::class.java)

   override suspend fun start() {

      val router = Router.router(vertx)
         .errorHandler(404) { it.end("Could not find path ${it.request().path()}") }

      val server = vertx.createHttpServer(options)
         .requestHandler(router)
         .exceptionHandler { logger.warn("Socket error", it) }
         .invalidRequestHandler {
            logger.warn("Invalid request")
            it.response().setStatusCode(400).end()
         }

      coroutineRouter {
         if (cohort.heapDump) {
            router.get("${cohort.endpointPrefix}/heapdump")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getHeapDump().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.memory) {
            router.get("${cohort.endpointPrefix}/memory")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getMemoryInfo().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         cohort.dataSources.let { dsm ->
            if (dsm.isNotEmpty()) {
               router.get("${cohort.endpointPrefix}/datasources")
                  .consumes("*")
                  .produces("*")
                  .coHandler { context ->
                     dsm.map { it.info() }.sequence().fold(
                        { context.json(it).coAwait() },
                        { context.response().setStatusCode(500).end().coAwait() },
                     )
                  }
            }
         }

         cohort.migrations?.let { m ->
            router.get("${cohort.endpointPrefix}/dbmigration")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  m.migrations().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         cohort.logManager?.let { manager ->

            router.get("${cohort.endpointPrefix}/logging")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  runCatching {
                     val levels = manager.levels()
                     val loggers = manager.loggers()
                     LogInfo(levels, loggers).toJson()
                  }.fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }

            router.put("${cohort.endpointPrefix}/logging/{name}/{level}")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  val name = context.pathParam("name")
                  val level = context.pathParam("level")
                  manager.set(name, level).fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.jvmInfo) {
            router.get("${cohort.endpointPrefix}/jvm")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getJvmDetails().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.gc) {
            router.get("${cohort.endpointPrefix}/gc")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getGcInfo().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.threadDump) {
            router.get("${cohort.endpointPrefix}/threaddump")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getThreadDump().fold(
                     { context.response().end(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.sysprops) {
            router.get("${cohort.endpointPrefix}/sysprops")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getSysProps().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         if (cohort.operatingSystem) {
            router.get("${cohort.endpointPrefix}/os")
               .consumes("*")
               .produces("*")
               .coHandler { context ->
                  getOperatingSystem().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
         }

         cohort.healthchecks.forEach { (endpoint, registry) ->
            logger.info("Deploying healthcheck at $endpoint")

            router.get(endpoint)
               .consumes("*")
               .produces("*")
               .coHandler { context ->

                  val status = registry.status()

                  val results = status.healthchecks.map {
                     ResultJson(
                        name = it.key,
                        status = it.value.result.status,
                        lastCheck = it.value.timestamp.atOffset(ZoneOffset.UTC).toString(),
                        message = it.value.result.message,
                        cause = it.value.result.cause?.stackTraceToString(),
                        consecutiveSuccesses = it.value.consecutiveSuccesses,
                        consecutiveFailures = it.value.consecutiveFailures,
                     )
                  }

                  when (status.healthy) {
                     true -> context.json(results).coAwait()
                     false -> context.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end().coAwait()
                  }
               }
         }
      }

      server.listen(port).toCompletionStage().await()
   }
}
