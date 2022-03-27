package com.sksamuel.cohort.db

interface DataSourceManager {
  fun info(): Result<DataSourceInfo>
}

data class DataSourceInfo(
  val name: String,
  val activeConnections: Int,
  val idleConnections: Int,
  val totalConnections: Int,
  val threadsAwaitingConnection: Int,
  val connectionTimeoutMs: Long,
  val idleTimeoutMs: Long,
  val maxLifetimeMs: Long,
  val leakDetectionThreshold: Long,
  val minIdle: Int,
  val maxOpenPreparedStatements: Int,
  val testOnReturn: Boolean?,
  val testOnBorrow: Boolean?,
  val testOnCreate: Boolean?,
  val maximumPoolSize: Int,
  val maximumIdle: Int,
  val validationTimeoutMs: Long,
)
