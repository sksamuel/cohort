package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

object Log4jManager : LogManager {

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
        effectiveLevel = it.level.name(),
      )
    }
  }

  override fun set(name: String, level: String): Result<Unit> = runCatching {
    Configurator.setLevel(name, Level.getLevel(level))
  }
}
