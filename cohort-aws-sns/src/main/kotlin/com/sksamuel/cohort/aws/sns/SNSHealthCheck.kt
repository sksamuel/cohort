package com.sksamuel.cohort.aws.sns

import com.amazonaws.services.sns.AmazonSNS
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AWS SNS by listing topics.
 */
class SNSHealthCheck(
  val createClient: () -> AmazonSNS,
) : HealthCheck {

  override val name: String = "aws_sns_topic"

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      createClient().listTopics()
    }.fold(
      { HealthCheckResult.Healthy("SNS access confirmed") },
      { HealthCheckResult.Unhealthy("Could not connect to SNS", it) }
    )
  }
}
