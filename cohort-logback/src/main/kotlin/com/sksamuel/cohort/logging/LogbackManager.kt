package com.sksamuel.cohort.logging

import ch.qos.logback.classic.LoggerContext
import org.slf4j.LoggerFactory

class LogbackManager : LogManager {

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

}
