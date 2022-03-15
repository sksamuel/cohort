package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import com.zaxxer.hikari.HikariDataSource

class HikariDataSourceManager(private vararg val datasources: HikariDataSource) : DataSourceManager {
  override fun details(): Result<List<DataSourceInfo>> {
    return runCatching {
      datasources.map { ds ->
        DataSourceInfo(
          name = ds.hikariConfigMXBean.poolName,
          activeConnections = ds.hikariPoolMXBean.activeConnections,
          idleConnections = ds.hikariPoolMXBean.idleConnections,
          totalConnections = ds.hikariPoolMXBean.totalConnections,
          threadsAwaitingConnection = ds.hikariPoolMXBean.threadsAwaitingConnection,
          connectionTimeout = ds.hikariConfigMXBean.connectionTimeout,
          idleTimeout = ds.hikariConfigMXBean.idleTimeout,
          maxLifetime = ds.hikariConfigMXBean.maxLifetime,
          leakDetectionThreshold = ds.hikariConfigMXBean.leakDetectionThreshold,
          maximumPoolSize = ds.hikariConfigMXBean.maximumPoolSize,
          validationTimeout = ds.hikariConfigMXBean.validationTimeout,
        )
      }
    }
  }
}
