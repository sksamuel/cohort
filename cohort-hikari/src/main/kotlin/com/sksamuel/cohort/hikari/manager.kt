package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import com.zaxxer.hikari.HikariDataSource

class HikariDataSourceManager(private val ds: HikariDataSource) : DataSourceManager {

  override fun name(): String = ds.hikariConfigMXBean.poolName

  override fun evict(): Result<Boolean> = runCatching {
    ds.hikariPoolMXBean.softEvictConnections()
    true
  }

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
        // Hikari always tests on borrow (uses JDBC4 isValid by default; connectionTestQuery
        // is only a legacy fallback string). Deriving testOnBorrow from connectionTestQuery
        // therefore reported `false` for the vast majority of Hikari pools even though every
        // borrow is validated.
        testOnBorrow = true,
        testOnReturn = null,
        // Hikari has no test-on-create concept. connectionInitSql is one-off initialization
        // SQL run when a new connection is created — not a test. Reporting testOnCreate=true
        // when only an init statement was configured was simply misleading.
        testOnCreate = null,
      )
    }
  }
}
