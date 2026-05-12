package com.sksamuel.cohort.aws.sqs

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AmazonSQS for the given queue.
 */
class SQSQueueHealthCheck(
   private val queue: String,
   val createClient: () -> AmazonSQS = { AmazonSQSClient.builder().build() },
   override val name: String = "aws_sqs_queue",
) : HealthCheck {

   private suspend fun use(client: AmazonSQS): Result<GetQueueUrlResult> {
      // `.also { client.shutdown() }` does not run if the inner runInterruptible block
      // is cancelled (it throws CancellationException before returning a value). Use
      // try/finally to guarantee the client is closed even on cancellation.
      return try {
         runInterruptible(Dispatchers.IO) {
            runCatching { client.getQueueUrl(queue) }
         }
      } finally {
         runCatching { client.shutdown() }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.healthy("SQS queue access confirmed $queue") },
            { HealthCheckResult.unhealthy("Could not connect to SQS queue $queue", it) }
         )
   }
}
