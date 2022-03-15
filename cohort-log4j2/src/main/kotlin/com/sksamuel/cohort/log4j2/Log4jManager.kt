package com.sksamuel.cohort.log4j2

import com.sksamuel.cohort.logging.LogManager
import com.sksamuel.cohort.logging.Logger

class Log4jManager : LogManager {
  override fun loggers(): List<Logger> {
    return org.apache.logging.log4j.LogManager.getContext().loggerRegistry.loggers.map {
      Logger(
        name = it.name,
        level = it.level.name(),
        effectiveLevel = it.level.name(),
      )
    }
  }
}
