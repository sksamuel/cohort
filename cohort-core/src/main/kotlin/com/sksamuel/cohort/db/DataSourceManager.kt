package com.sksamuel.cohort.db

interface DataSourceManager {
  fun details(): Result<List<DataSourceInfo>>
}

data class DataSourceInfo(
  val name: String,
  val activeConnections: Int,
  val idleConnections: Int,
  val totalConnections: Int,
  val threadsAwaitingConnection: Int,
  val connectionTimeout: Long,
  val idleTimeout: Long,
  val maxLifetime: Long,
  val leakDetectionThreshold: Long,
  val maximumPoolSize: Int,
  val validationTimeout: Long
)
