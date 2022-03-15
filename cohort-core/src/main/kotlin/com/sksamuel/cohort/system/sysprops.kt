package com.sksamuel.cohort.system

fun getSysProps(): Result<Map<String, String>> = runCatching {
  System.getProperties().map { it.key.toString() to it.value.toString() }.toMap()
}
