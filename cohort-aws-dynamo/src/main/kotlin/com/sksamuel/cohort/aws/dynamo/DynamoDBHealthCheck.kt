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
   private val client: AmazonDynamoDB,
   override val name: String = "aws_dynamodb",
) : HealthCheck {

   constructor(
      createClient: () -> AmazonDynamoDB = { AmazonDynamoDBClient.builder().build() },
      name: String = "aws_dynamodb",
   ) : this(createClient(), name)

   override suspend fun check(): HealthCheckResult {
      return runInterruptible(Dispatchers.IO) {
         runCatching {
            client.listTables(1)
         }
      }.fold(
         { HealthCheckResult.healthy("DynamoDB access successful") },
         { HealthCheckResult.unhealthy("Could not connect to DynamoDB", it) }
      )
   }
}
