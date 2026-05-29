package com.sksamuel.cohort.dbcp

import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.DataSourceManager
import org.apache.commons.dbcp2.BasicDataSource

class ApacheDBCPDataSourceManager(private val ds: BasicDataSource) : DataSourceManager {

  override fun name(): String = ds.jmxName

  override fun evict(): Result<Boolean> = runCatching {
    ds.evict()
    true
  }

  override fun info(): Result<DataSourceInfo> {
    return runCatching {
      DataSourceInfo(
        name = ds.jmxName,
        activeConnections = ds.numActive,
        idleConnections = ds.numIdle,
        totalConnections = ds.numActive + ds.numIdle,
        threadsAwaitingConnection = -1,
        // DataSource.loginTimeout and BasicDataSource.validationQueryTimeout are in seconds
        // (per CommonDataSource / DBCP2), while the DataSourceInfo fields are documented as
        // milliseconds. Convert so observability shows the real values.
        connectionTimeoutMs = ds.loginTimeout.toLong() * 1000,
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
        validationTimeoutMs = ds.validationQueryTimeout.toLong() * 1000,
      )
    }
  }

}
