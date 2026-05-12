package com.sksamuel.cohort.aws.sns

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.ListTopicsResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AWS SNS by listing topics.
 */
class SNSHealthCheck(
   val createClient: () -> AmazonSNS = { AmazonSNSClient.builder().build() },
   override val name: String = "aws_sns_topic",
) : HealthCheck {

   private suspend fun use(client: AmazonSNS): Result<ListTopicsResult> {
      // `.also { client.shutdown() }` does not run if the inner runInterruptible block
      // is cancelled (it throws CancellationException before returning a value). Use
      // try/finally to guarantee the client is closed even on cancellation.
      return try {
         runInterruptible(Dispatchers.IO) {
            runCatching { client.listTopics() }
         }
      } finally {
         runCatching { client.shutdown() }
      }
   }

   override suspend fun check(): HealthCheckResult {
      return runCatching { createClient() }
         .flatMap { use(it) }
         .fold(
            { HealthCheckResult.healthy("SNS access confirmed") },
            { HealthCheckResult.unhealthy("Could not connect to SNS", it) }
         )
   }
}
