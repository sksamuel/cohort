package com.sksamuel.cohort

import com.sksamuel.cohort.endpoints.CohortConfiguration
import com.sksamuel.cohort.endpoints.cohort
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey

val CohortConfigAttributeKey: AttributeKey<CohortConfiguration> = AttributeKey("CohortConfigAttributeKey")

val Cohort = createApplicationPlugin(name = "Cohort", createConfiguration = ::CohortConfiguration) {
  val config: CohortConfiguration = this@createApplicationPlugin.pluginConfig
  this.application.attributes.put(CohortConfigAttributeKey, config)
  application.routing { cohort() }
}
