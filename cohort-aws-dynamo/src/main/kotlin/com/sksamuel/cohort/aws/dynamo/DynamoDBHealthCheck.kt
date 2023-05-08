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
) : HealthCheck {

   override val name: String = "aws_dynamodb"

   private fun <T> AmazonDynamoDB.use(f: (AmazonDynamoDB) -> T): Result<T> {
      val result = runCatching { f(this) }
      this.shutdown()
      return result
   }

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         createClient().use {
            it.listTables(1)
         }
      }.fold(
         { HealthCheckResult.healthy("DynamoDB access successful") },
         { HealthCheckResult.unhealthy("Could not connect to DynamoDB", it) }
      )
   }
}
