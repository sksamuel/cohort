package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import com.zaxxer.hikari.HikariDataSource

class HikariDataSourceManager(private val ds: HikariDataSource) : DataSourceManager {
  override fun details() = runCatching {
    DataSourceInfo(
      ds.hikariPoolMXBean.activeConnections,
      ds.hikariPoolMXBean.idleConnections,
      ds.hikariPoolMXBean.totalConnections,
      ds.hikariPoolMXBean.threadsAwaitingConnection,
      ds.hikariConfigMXBean.connectionTimeout,
      ds.hikariConfigMXBean.idleTimeout,
      ds.hikariConfigMXBean.maxLifetime,
      ds.hikariConfigMXBean.leakDetectionThreshold,
      ds.hikariConfigMXBean.maximumPoolSize,
      ds.hikariConfigMXBean.poolName,
      ds.hikariConfigMXBean.validationTimeout,
    )
  }
}
