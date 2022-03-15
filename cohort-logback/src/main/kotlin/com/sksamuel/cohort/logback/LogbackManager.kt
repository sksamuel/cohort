package com.sksamuel.cohort.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
import org.slf4j.LoggerFactory

class LogbackManager : LogManager {

  override fun levels() =
    listOf(Level.DEBUG, Level.TRACE, Level.INFO, Level.ERROR, Level.OFF, Level.WARN).map { it.levelStr }

  override fun loggers(): List<Logger> {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    return context.loggerList.map {
      Logger(
        name = it.name,
        level = it.level.levelStr,
        effectiveLevel = it.effectiveLevel.levelStr,
      )
    }
  }

  override fun set(name: String, level: String) = runCatching {
    val context = LoggerFactory.getILoggerFactory() as LoggerContext
    val logger = context.getLogger(name)
    logger.level = Level.toLevel(level)
  }
}
