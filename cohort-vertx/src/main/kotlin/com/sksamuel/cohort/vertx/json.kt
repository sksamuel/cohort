package com.sksamuel.cohort.vertx

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sksamuel.cohort.HealthStatus
import com.sksamuel.cohort.db.DataSourceInfo
import com.sksamuel.cohort.db.Migration
import com.sksamuel.cohort.gc.GCInfo
import com.sksamuel.cohort.jvm.Jvm
import com.sksamuel.cohort.logging.LogInfo
import com.sksamuel.cohort.memory.MemoryInfo
import com.sksamuel.cohort.os.OperatingSystem
import com.sksamuel.cohort.system.SysProps

data class ResultJson(
  val name: String,
  val status: HealthStatus,
  val lastCheck: String,
  val message: String?,
  val cause: String?,
  val consecutiveSuccesses: Int,
  val consecutiveFailures: Int,
)

@JvmName("ResultJsonToJson")
fun List<ResultJson>.toJson(): String = mapper.writeValueAsString(this)

@JvmName("DataSourceInfoToJson")
fun List<DataSourceInfo>.toJson(): String = mapper.writeValueAsString(this)

@JvmName("MigrationToJson")
fun List<Migration>.toJson(): String = mapper.writeValueAsString(this)

fun OperatingSystem.toJson(): String = mapper.writeValueAsString(this)
fun LogInfo.toJson(): String = mapper.writeValueAsString(this)
fun Jvm.toJson(): String = mapper.writeValueAsString(this)
fun SysProps.toJson(): String = mapper.writeValueAsString(this)
fun GCInfo.toJson(): String = mapper.writeValueAsString(this)
fun MemoryInfo.toJson(): String = mapper.writeValueAsString(this)

val mapper = jacksonObjectMapper()
