package com.sksamuel.cohort.aws.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AWS Dynamo DB instance
 * by connecting and requesting to list the tables (limit of 1).
 */
class DynamoDBHealthCheck(
   val createClient: () -> AmazonDynamoDB = { AmazonDynamoDBClient.builder().build() },
   override val name: String = "aws_dynamodb",
) : HealthCheck {

   private fun <T> AmazonDynamoDB.use(f: (AmazonDynamoDB) -> T): Result<T> {
      val result = runCatching { f(this) }
      runCatching { this.shutdown() }
      return result
   }

   override suspend fun check(): HealthCheckResult {
      // Catch errors from createClient() itself (AWS region/credential resolution can throw
      // synchronously on builder().build()). Previously such failures escaped runInterruptible
      // and propagated out of check() instead of producing an Unhealthy result.
      return runCatching {
         runInterruptible(Dispatchers.IO) {
            createClient().use {
               it.listTables(1)
            }
         }
      }.fold(
         { it.fold(
            { HealthCheckResult.healthy("DynamoDB access successful") },
            { HealthCheckResult.unhealthy("Could not connect to DynamoDB", it) }
         )},
         { HealthCheckResult.unhealthy("Could not connect to DynamoDB", it) },
      )
   }
}
