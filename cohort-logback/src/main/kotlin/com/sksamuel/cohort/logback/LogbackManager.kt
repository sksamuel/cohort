package com.sksamuel.cohort.logback

import ch.qos.logback.classic.LoggerContext
import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger
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
