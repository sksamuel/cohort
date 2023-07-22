package com.sksamuel.cohort.endpoints

import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.WarmupRegistry
import com.sksamuel.cohort.db.DataSourceManager
import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.logging.LogManager

class CohortConfiguration {

   internal val healthchecks = mutableMapOf<String, HealthCheckRegistry>()
   private var warmupRegistry: WarmupRegistry? = null

   // set to true to enable the /cohort/heapdump endpoint which will generate a heapdump in hprof format
   var heapDump: Boolean = false

   // set to true to enable the /cohort/os endpoint which returns operating system information
   var operatingSystem: Boolean = false

   // set to true to enable the /cohort/memory endpoint which returns memory pool information
   var memory: Boolean = false

   // set to true to enable the /cohort/log endpoint which returns log system information
   var logManager: LogManager? = null

   // register one or more DataSourceManagers to show information database pools
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

   var endpointPrefix = "cohort"

   /**
    * Register a [HealthCheckRegistry] at the given [endpoint].
    */
   fun healthcheck(endpoint: String, registry: HealthCheckRegistry) {
      registry.warmupRegistry = warmupRegistry
      healthchecks[endpoint] = registry
   }

   /**
    * Register a [WarmupRegistry].
    */
   fun warmup(registry: WarmupRegistry) {
      if (healthchecks.isNotEmpty()) error("WarmupRegister must be registered before healthchecks")
      if (warmupRegistry != null) error("WarmupRegistry already registered")
      this.warmupRegistry = registry
   }

   /**
    * Creates a [WarmupRegistry], registers it, and executes the provided [configure] function
    * against the new registry.
    */
   fun warmups(configure: WarmupRegistry.() -> Unit) {
      val registry = WarmupRegistry()
      configure.invoke(registry)
      warmup(registry)
   }
}
