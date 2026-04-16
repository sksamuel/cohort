rootProject.name = "cohort"

include(
   "cohort-api",
   "cohort-aws-dynamo",
   "cohort-aws-s3",
   "cohort-aws-sqs",
   "cohort-aws-sns",
   "cohort-cassandra",
   "cohort-dbcp",
   "cohort-elastic",
   "cohort-flyway",
   "cohort-hikari",
   "cohort-jedis",
   "cohort-kafka",
   "cohort-ktor",
   "cohort-lettuce",
   "cohort-liquibase",
   "cohort-log4j2",
   "cohort-logback",
   "cohort-micrometer",
   "cohort-mongo",
   "cohort-pulsar",
   "cohort-rabbit",
   "cohort-vertx",
)

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
   repositories {
      mavenLocal()
      mavenCentral()
   }
   versionCatalogs {
      create("libs") {

         val micrometer = "1.16.5"
         library("micrometer-core", "io.micrometer:micrometer-core:$micrometer")

         val hikari = "6.3.3"
         library("hikari", "com.zaxxer:HikariCP:$hikari")

         val flyway = "11.3.2"
         library("flyway-core", "org.flywaydb:flyway-core:$flyway")

         val kafka = "3.9.0"
         library("kafka-client", "org.apache.kafka:kafka-clients:$kafka")

         val lettuce = "6.8.2.RELEASE"
         library("lettuce-core", "io.lettuce:lettuce-core:$lettuce")

         val liquibase = "4.31.1"
         library("liquibase-core", "org.liquibase:liquibase-core:$liquibase")

         val jedis = "5.2.0"
         library("jedis", "redis.clients:jedis:$jedis")

         library("rabbitmq", "com.rabbitmq:amqp-client:5.26.0")
         library("dbcp2", "org.apache.commons:commons-dbcp2:2.13.0")
         library("pulsar-client", "org.apache.pulsar:pulsar-client:4.0.4")

         val mongo = "5.3.0"
         library("mongodb-driver-sync", "org.mongodb:mongodb-driver-sync:$mongo")
         library("mongodb-driver-coroutine", "org.mongodb:mongodb-driver-kotlin-coroutine:$mongo")

         library(
            "elasticsearch-rest-high-level-client",
            "org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.23"
         )

         library("elasticsearch-java", "co.elastic.clients:elasticsearch-java:8.17.2")

         val awssdk = "1.12.797"
         library("aws-java-sdk-dynamodb", "com.amazonaws:aws-java-sdk-dynamodb:$awssdk")
         library("aws-java-sdk-s3", "com.amazonaws:aws-java-sdk-s3:$awssdk")
         library("aws-java-sdk-sns", "com.amazonaws:aws-java-sdk-sns:$awssdk")
         library("aws-java-sdk-sqs", "com.amazonaws:aws-java-sdk-sqs:$awssdk")

         val log4j2 = "2.25.0"
         library("log4j2-api", "org.apache.logging.log4j:log4j-api:$log4j2")
         library("log4j2-core", "org.apache.logging.log4j:log4j-core:$log4j2")
         library("log4j2-slf4j2-impl", "org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2")

         val ktor = "3.2.3"
         library("ktor-client-apache5", "io.ktor:ktor-client-apache5:$ktor")
         library("ktor-server-host-common", "io.ktor:ktor-server-host-common:$ktor")
         library("ktor-server-netty", "io.ktor:ktor-server-netty:$ktor")
         library("ktor-server-test-host", "io.ktor:ktor-server-test-host:$ktor")

         val logback = "1.5.19"
         library("logback-classic", "ch.qos.logback:logback-classic:$logback")
         library("logback-core", "ch.qos.logback:logback-core:$logback")

         val jackson = "2.21.1"
         library("jackson-core", "com.fasterxml.jackson.core:jackson-core:$jackson")
         library("jackson-datatype-jsr310","com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
         library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")

         val testContainers = "1.21.1"
         library("testcontainers", "org.testcontainers:testcontainers:$testContainers")
         library("testcontainers-postgresql", "org.testcontainers:postgresql:$testContainers")
         library("testcontainers-rabbitmq", "org.testcontainers:rabbitmq:$testContainers")
         library("testcontainers-elasticsearch", "org.testcontainers:elasticsearch:$testContainers")
         library("testcontainers-mongodb", "org.testcontainers:mongodb:$testContainers")
         library("testcontainers-kafka", "org.testcontainers:kafka:$testContainers")
         library("testcontainers-cassandra", "org.testcontainers:cassandra:$testContainers")

         val vertx = "4.5.26"
         library("vertx-core", "io.vertx:vertx-core:$vertx")
         library("vertx-web", "io.vertx:vertx-web:$vertx")
         library("vertx-kotlin", "io.vertx:vertx-lang-kotlin:$vertx")
         library("vertx-coroutines", "io.vertx:vertx-lang-kotlin-coroutines:$vertx")
         library("vertx-micrometer", "io.vertx:vertx-micrometer-metrics:$vertx")

         val cassandra = "4.19.1"
         library("cassandra", "org.apache.cassandra:java-driver-core:$cassandra")

         val mockk = "1.14.3"
         library("mockk", "io.mockk:mockk:$mockk")
      }
   }
}

