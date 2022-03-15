package com.sksamuel.cohort.logging

interface LogManager {
  fun loggers(): List<Logger>
  fun set(name: String, level: String): Result<Unit>
  fun levels(): List<String>
}

data class Logger(
  val name: String,
  val level: String,
)
