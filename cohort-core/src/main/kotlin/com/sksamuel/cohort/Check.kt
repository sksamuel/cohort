package com.sksamuel.cohort

interface Check {
  suspend fun check(): CheckResult
}

sealed class CheckResult {

  val isHealthy: Boolean by lazy { this is Healthy }
  abstract val message: String?
  abstract val cause: Throwable?

  data class Healthy(override val message: String?) : CheckResult() {
    override val cause: Throwable? = null
  }

  data class Unhealthy(override val message: String, override val cause: Throwable?) : CheckResult()
}
