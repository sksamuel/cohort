package com.sksamuel.cohort.endpoints

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.db.DataSourceManager
import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.logging.LogManager

class CohortConfiguration {

   private val _healthchecks = mutableMapOf<String, HealthCheckRegistry>()
   val healthchecks: Map<String, HealthCheckRegistry> get() = _healthchecks

   // set to true to enable the /cohort/heapdump endpoint which will generate a heapdump in hprof format
   var heapDump: Boolean = false

   // set to true to enable the /cohort/os endpoint which returns operating system information
   var operatingSystem: Boolean = false

   // set to true to enable the /cohort/memory endpoint which returns memory pool information
   var memory: Boolean = false

   // set to a non-null LogManager to enable the /cohort/logging endpoints which return and update logger levels
   var logManager: LogManager? = null

   // register one or more DataSourceManagers to show information about database pools
   var dataSources: List<DataSourceManager> = emptyList()

   // register one or more DatabaseMigrationManagers to show information about database migrations
   var migrations: DatabaseMigrationManager? = null

   // set to true to enable the /cohort/jvm endpoint which returns JVM information
   var jvmInfo: Boolean = false

   // set to true to enable the /cohort/gc endpoint which returns garbage collector times and counts
   var gc: Boolean = false

   // set to true to enable the /cohort/threaddump endpoint which returns a thread dump
   var threadDump: Boolean = false

   // set to true to enable the /cohort/sysprops endpoint which returns current system properties
   var sysprops: Boolean = false

   // set to true to return the detailed status of the healthcheck response.
   // @Volatile because the field is read from the request-handler thread (Netty event loop /
   // Vert.x worker) but written from the configure { } block on a different thread.
   @Volatile var verboseHealthCheckResponse: Boolean = true

   // Path prefix for cohort's endpoints. MUST start with `/` — Vert.x's Router rejects route
   // patterns that don't start with a slash, so the previous default of `"cohort"` caused
   // any Vert.x app enabling cohort to throw at startup with the default config.
   var endpointPrefix = "/cohort"

   /**
    * Register a [HealthCheckRegistry] at the given [endpoint].
    *
    * Throws if an endpoint has already been registered, mirroring the duplicate-name behaviour
    * of `HealthCheckRegistry.register`. The previous silent overwrite made typo'd endpoint
    * strings impossible to diagnose.
    */
   fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
      require(endpoint.startsWith("/")) { "endpoint must start with '/' but was '$endpoint'" }
      require(!_healthchecks.containsKey(endpoint)) { "Endpoint $endpoint already registered" }
      _healthchecks[endpoint] = registry
   }
}
