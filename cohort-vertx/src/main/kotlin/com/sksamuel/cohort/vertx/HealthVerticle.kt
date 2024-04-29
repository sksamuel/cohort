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
import io.vertx.ext.web.Router
import org.slf4j.LoggerFactory
import java.time.ZoneOffset

fun Router.cohort(
   configure: CohortConfiguration.() -> Unit = {},
) {

   val router = this
   val logger = LoggerFactory.getLogger(Router::class.java)
   val cohort = CohortConfiguration().also(configure)

   if (cohort.heapDump) {
      router.get("${cohort.endpointPrefix}/heapdump")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getHeapDump().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.memory) {
      router.get("${cohort.endpointPrefix}/memory")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getMemoryInfo().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   cohort.dataSources.let { dsm ->
      if (dsm.isNotEmpty()) {
         router.get("${cohort.endpointPrefix}/datasources")
            .consumes("*")
            .produces("*")
            .handler { context ->
               dsm.map { it.info() }.sequence().fold(
                  { context.json(it) },
                  { context.response().setStatusCode(500).end() },
               )
            }
      }
   }

   cohort.migrations?.let { m ->
      router.get("${cohort.endpointPrefix}/dbmigration")
         .consumes("*")
         .produces("*")
         .handler { context ->
            m.migrations().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   cohort.logManager?.let { manager ->

      router.get("${cohort.endpointPrefix}/logging")
         .consumes("*")
         .produces("*")
         .handler { context ->
            runCatching {
               val levels = manager.levels()
               val loggers = manager.loggers()
               LogInfo(levels, loggers).toJson()
            }.fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }

      router.put("${cohort.endpointPrefix}/logging/{name}/{level}")
         .consumes("*")
         .produces("*")
         .handler { context ->
            val name = context.pathParam("name")
            val level = context.pathParam("level")
            manager.set(name, level).fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.jvmInfo) {
      router.get("${cohort.endpointPrefix}/jvm")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getJvmDetails().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.gc) {
      router.get("${cohort.endpointPrefix}/gc")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getGcInfo().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.threadDump) {
      router.get("${cohort.endpointPrefix}/threaddump")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getThreadDump().fold(
               { context.response().end(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.sysprops) {
      router.get("${cohort.endpointPrefix}/sysprops")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getSysProps().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   if (cohort.operatingSystem) {
      router.get("${cohort.endpointPrefix}/os")
         .consumes("*")
         .produces("*")
         .handler { context ->
            getOperatingSystem().fold(
               { context.json(it) },
               { context.response().setStatusCode(500).end() },
            )
         }
   }

   cohort.healthchecks.forEach { (endpoint, registry) ->
      logger.info("Deploying healthcheck at $endpoint")

      router.get(endpoint)
         .consumes("*")
         .produces("*")
         .handler { context ->

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

            registry.logUnhealthy

            when (status.healthy) {
               true -> context.json(results)
               false -> context.response().setStatusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code()).end()
            }
         }
   }
}
