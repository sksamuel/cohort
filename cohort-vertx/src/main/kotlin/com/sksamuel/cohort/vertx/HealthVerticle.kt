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
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.coroutineRouter
import java.time.ZoneOffset

class HealthVerticle(
   private val router: Router,
   configure: CohortConfiguration.() -> Unit = {},
) : CoroutineVerticle() {

   private val cohort = CohortConfiguration().also(configure)

   override suspend fun start() {
      coroutineRouter {
         if (cohort.heapDump) {
            router.get("${cohort.endpointPrefix}/heapdump").coHandler { context ->
               getHeapDump().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.memory) {
            router.get("${cohort.endpointPrefix}/memory").coHandler { context ->
               getMemoryInfo().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         cohort.dataSources.let { dsm ->
            if (dsm.isNotEmpty()) {
               router.get("${cohort.endpointPrefix}/datasources").coHandler { context ->
                  dsm.map { it.info() }.sequence().fold(
                     { context.json(it).coAwait() },
                     { context.response().setStatusCode(500).end().coAwait() },
                  )
               }
            }
         }

         cohort.migrations?.let { m ->
            router.get("${cohort.endpointPrefix}/dbmigration").coHandler { context ->
               m.migrations().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         cohort.logManager?.let { manager ->

            router.get("${cohort.endpointPrefix}/logging").coHandler { context ->
               runCatching {
                  val levels = manager.levels()
                  val loggers = manager.loggers()
                  LogInfo(levels, loggers).toJson()
               }.fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }

            router.put("${cohort.endpointPrefix}/logging/{name}/{level}").coHandler { context ->
               val name = context.pathParam("name")
               val level = context.pathParam("level")
               manager.set(name, level).fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.jvmInfo) {
            router.get("${cohort.endpointPrefix}/jvm").coHandler { context ->
               getJvmDetails().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.gc) {
            router.get("${cohort.endpointPrefix}/gc").coHandler { context ->
               getGcInfo().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.threadDump) {
            router.get("${cohort.endpointPrefix}/threaddump").coHandler { context ->
               getThreadDump().fold(
                  { context.response().end(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.sysprops) {
            router.get("${cohort.endpointPrefix}/sysprops").coHandler { context ->
               getSysProps().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         if (cohort.operatingSystem) {
            router.get("${cohort.endpointPrefix}/os").coHandler { context ->
               getOperatingSystem().fold(
                  { context.json(it).coAwait() },
                  { context.response().setStatusCode(500).end().coAwait() },
               )
            }
         }

         cohort.healthchecks.forEach { (endpoint, registry) ->
            router.get(endpoint).coHandler { context ->

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

               val httpStatusCode = when (status.healthy) {
                  true -> HttpResponseStatus.OK.code()
                  false -> HttpResponseStatus.SERVICE_UNAVAILABLE.code()
               }

               context.response().setStatusCode(httpStatusCode)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                  .end(Json.encodeToBuffer(results))
                  .coAwait()
            }
         }
      }
   }
}
