package com.sksamuel.cohort.aws.dynamo

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult

/**
 * A Cohort [HealthCheck] that checks for connectivity to an AWS Dynamo DB instance.
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
      return createClient().use {
         it.listTables()
      }.fold(
         { HealthCheckResult.Healthy("DynamoDB access successful") },
         { HealthCheckResult.Unhealthy("Could not connect to DynamoDB", it) }
      )
   }
}
