package com.sksamuel.cohort.config

interface ConfigProvider {
  suspend fun config(): Result<Any>
}
