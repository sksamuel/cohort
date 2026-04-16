package com.sksamuel.cohort.aws.sqs

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AmazonSQS for the given queue.
 */
class SQSQueueHealthCheck(
   private val queue: String,
   private val client: AmazonSQS,
   override val name: String = "aws_sqs_queue",
) : HealthCheck {

   constructor(
      queue: String,
      createClient: () -> AmazonSQS = { AmazonSQSClient.builder().build() },
      name: String = "aws_sqs_queue",
   ) : this(queue, createClient(), name)

   override suspend fun check(): HealthCheckResult {
      return runCatching { client.getQueueUrl(queue) }
         .fold(
            { HealthCheckResult.healthy("SQS queue access confirmed $queue") },
            { HealthCheckResult.unhealthy("Could not connect to SQS queue $queue", it) }
         )
   }
}
