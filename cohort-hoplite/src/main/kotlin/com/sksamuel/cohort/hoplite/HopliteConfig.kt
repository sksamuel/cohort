package com.sksamuel.cohort.hoplite

import com.sksamuel.cohort.config.ConfigProvider
import com.sksamuel.hoplite.ConfigLoader

class HopliteConfig(private val loader: ConfigLoader) : ConfigProvider {
  override suspend fun config(): Result<Any> = runCatching {
    return loader.loadConfigOrThrow()
  }
}
