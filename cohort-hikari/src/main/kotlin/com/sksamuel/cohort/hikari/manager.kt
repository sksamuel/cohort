package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import com.zaxxer.hikari.HikariDataSource

class HikariDataSourceManager(private val ds: HikariDataSource) : DataSourceManager {
  override fun info(): Result<DataSourceInfo> {
    return runCatching {
      DataSourceInfo(
        name = ds.hikariConfigMXBean.poolName,
        activeConnections = ds.hikariPoolMXBean.activeConnections,
        idleConnections = ds.hikariPoolMXBean.idleConnections,
        totalConnections = ds.hikariPoolMXBean.totalConnections,
        threadsAwaitingConnection = ds.hikariPoolMXBean.threadsAwaitingConnection,
        connectionTimeoutMs = ds.hikariConfigMXBean.connectionTimeout,
        idleTimeoutMs = ds.hikariConfigMXBean.idleTimeout,
        maxLifetimeMs = ds.hikariConfigMXBean.maxLifetime,
        leakDetectionThreshold = ds.hikariConfigMXBean.leakDetectionThreshold,
        maximumPoolSize = ds.hikariConfigMXBean.maximumPoolSize,
        validationTimeoutMs = ds.hikariConfigMXBean.validationTimeout,
        maximumIdle = -1,
        minIdle = ds.minimumIdle,
        maxOpenPreparedStatements = -1,
        testOnBorrow = ds.connectionTestQuery != null,
        testOnReturn = null,
        testOnCreate = ds.connectionInitSql != null,
      )
    }
  }
}
