package com.sksamuel.cohort.hoplite

import com.sksamuel.cohort.config.ConfigProvider
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.Node

class HopliteConfigProvider(private val loader: ConfigLoader) : ConfigProvider {
  override suspend fun config(): Result<Node> = runCatching {
    loader.loadNodeOrThrow()
  }
}
