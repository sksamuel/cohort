package com.sksamuel.cohort.endpoints

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.db.DataSourceManager
import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.shutdown.ShutdownHook

class CohortConfiguration {

  val healthchecks = mutableMapOf<String, HealthCheckRegistry>()
  val hooks = mutableListOf<ShutdownHook>()

  // set to true to enable the /cohort/heapdump endpoint which will generate a heapdump in hprof format
  var heapDump: Boolean = false

  // set to true to enable the /cohort/os endpoint which returns operating system information
  var operatingSystem: Boolean = false

  // set to true to enable the /cohort/memory endpoint which returns memory pool information
  var memory: Boolean = false

  var logManager: LogManager? = null

  var dataSources: List<DataSourceManager> = emptyList()

  var migrations: DatabaseMigrationManager? = null

  // set to true to enable the /cohort/jvm endpoint which returns JVM information
  var jvmInfo: Boolean = false

  // set to true to enable the /cohort/gc endpoint which returns garbage collector times and counts
  var gc: Boolean = false

  // set to true to enable the /cohort/threaddump endpoint which returns a thread dump
  var threadDump: Boolean = false

  // set to true to enable the /cohort/sysprops endpoint which returns current system properties
  var sysprops: Boolean = false

  fun shutdown(f: suspend () -> Unit) = shutdown(ShutdownHook(f))
  fun shutdown(hook: ShutdownHook) {
    hooks.add(hook)
  }

  fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
    healthchecks[endpoint] = registry
  }
}
