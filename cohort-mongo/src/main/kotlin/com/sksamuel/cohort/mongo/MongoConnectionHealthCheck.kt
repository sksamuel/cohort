package com.sksamuel.cohort.mongo

import com.mongodb.client.MongoClient
import com.sksamuel.cohort.HealthCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runInterruptible

class MongoConnectionHealthCheck private constructor(
   private val delegate: GenericMongoConnectionHealthCheck,
) : HealthCheck by delegate {

   constructor(
      client: MongoClient,
      name: String = "mongo_connection",
   ) : this(
      GenericMongoConnectionHealthCheck(name) {
         runInterruptible(Dispatchers.IO) {
            client.listDatabaseNames().toList()
         }
      }
   )

   constructor(
      client: com.mongodb.kotlin.client.coroutine.MongoClient,
      name: String = "mongo_connection",
   ) : this(
      GenericMongoConnectionHealthCheck(name) {
         client.listDatabaseNames().toList()
      },
   )
}
