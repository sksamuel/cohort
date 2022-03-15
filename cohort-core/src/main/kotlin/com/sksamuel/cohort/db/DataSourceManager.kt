package com.sksamuel.cohort.db

interface DataSourceManager {
  fun details(): Result<DataSourceInfo>
}

data class DataSourceInfo(
  val activeConnections: Int,
  val idleConnections: Int,
  val totalConnections: Int,
  val threadsAwaitingConnection: Int,
  val connectionTimeout: Long,
  val idleTimeout: Long,
  val maxLifetime: Long,
  val leakDetectionThreshold: Long,
  val maximumPoolSize: Int,
  val poolName: String,
  val validationTimeout: Long
)
