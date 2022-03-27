package com.sksamuel.cohort.hikari

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import org.apache.commons.dbcp2.BasicDataSource

class ApacheDBCPDataSourceManager(private val ds: BasicDataSource) : DataSourceManager {
  override fun info(): Result<DataSourceInfo> {
    return runCatching {
      DataSourceInfo(
        name = ds.jmxName,
        activeConnections = ds.numActive,
        idleConnections = ds.numIdle,
        totalConnections = ds.numActive + ds.numIdle,
        threadsAwaitingConnection = -1,
        connectionTimeoutMs = ds.loginTimeout.toLong(),
        idleTimeoutMs = ds.minEvictableIdleTimeMillis,
        maxLifetimeMs = ds.maxConnLifetimeMillis,
        leakDetectionThreshold = -1,
        minIdle = ds.minIdle,
        maxOpenPreparedStatements = ds.maxOpenPreparedStatements,
        testOnReturn = ds.testOnReturn,
        testOnBorrow = ds.testOnBorrow,
        testOnCreate = ds.testOnCreate,
        maximumPoolSize = ds.maxTotal,
        maximumIdle = ds.maxIdle,
        validationTimeoutMs = ds.validationQueryTimeout.toLong(),
      )
    }
  }
}
