package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

object Log4j2Manager : LogManager {

  override fun levels(): List<String> {
    return listOf(
      Level.DEBUG,
      Level.ERROR,
      Level.OFF,
      Level.WARN,
      Level.TRACE,
      Level.INFO,
      Level.FATAL
    ).map { it.name() }
  }

  override fun loggers(): List<Logger> {
    return org.apache.logging.log4j.LogManager.getContext(false).loggerRegistry.loggers.map {
      Logger(
        name = it.name,
        level = it.level.name(),
      )
    }
  }

  override fun set(name: String, level: String): Result<Unit> = runCatching {
    // Level.getLevel returns null for unknown level strings. Configurator.setLevel(name, null)
    // would silently no-op (or reset the level) and the runCatching block would still report
    // success. Fail loudly instead, so a typo via PUT /logging/{name}/{level} surfaces as an error.
    val resolved = Level.getLevel(level) ?: error("Unknown log level: $level")
    Configurator.setLevel(name, resolved)
  }
}
