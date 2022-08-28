package com.sksamuel.cohort.aws.sqs

import com.amazonaws.services.sqs.AmazonSQS
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AmazonSQS for the given queue.
 */
class SQSQueueHealthCheck(
  private val queue: String,
  val createClient: () -> AmazonSQS,
) : HealthCheck {

  override val name: String = "aws_sqs_queue"

  override suspend fun check(): HealthCheckResult {
    return runCatching {
      createClient().getQueueUrl(queue)
    }.fold(
      { HealthCheckResult.Healthy("SQS queue access confirmed $queue") },
      { HealthCheckResult.Unhealthy("Could not connect to SQS queue $queue", it) }
    )
  }
}
