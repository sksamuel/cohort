package com.sksamuel.cohort.ktor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class ResultJson(
  val name: String,
  val healthy: Boolean,
  val lastCheck: String,
  val message: String?,
  val cause: String?,
  val consecutiveSuccesses: Int,
  val consecutiveFailures: Int,
)

val mapper = jacksonObjectMapper()
