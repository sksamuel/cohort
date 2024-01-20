package com.sksamuel.cohort.system

fun getSysProps(): Result<SysProps> = runCatching {
  val map = System.getProperties().map { it.key.toString() to it.value.toString() }.toMap()
  SysProps(map)
}

data class SysProps(
  val properties: Map<String, String>,
)
