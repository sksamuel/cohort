package com.sksamuel.cohort.logging

interface LogManager {
  fun loggers(): List<Logger>
}

data class Logger(
  val name: String,
  val level: String,
  val effectiveLevel: String,
)
