package com.sksamuel.cohort.aws.sns

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.ListTopicsResult
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.tabby.results.flatMap

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AWS SNS by listing topics.
 */
class SNSHealthCheck(
   val createClient: () -> AmazonSNS = { AmazonSNSClient.builder().build() },
) : HealthCheck {

   override val name: String = "aws_sns_topic"

   private fun use(client: AmazonSNS): Result<ListTopicsResult> {
      return runCatching { client.listTopics() }.also { client.shutdown() }
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
