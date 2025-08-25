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

         val micrometer = "1.14.5"
         library("micrometer-core", "io.micrometer:micrometer-core:$micrometer")

         val hikari = "6.3.0"
         library("hikari", "com.zaxxer:HikariCP:$hikari")

         val flyway = "10.17.0"
         library("flyway-core", "org.flywaydb:flyway-core:$flyway")

         val kakfa = "3.8.0"
         library("kafka-client", "org.apache.kafka:kafka-clients:$kakfa")

         val lettuce = "6.5.5.RELEASE"
         library("lettuce-core", "io.lettuce:lettuce-core:$lettuce")

         val liquibase = "4.31.1"
         library("liquibase-core", "org.liquibase:liquibase-core:$liquibase")

         val jedis = "5.1.4"
         library("jedis", "redis.clients:jedis:$jedis")

         library("rabbitmq", "com.rabbitmq:amqp-client:5.25.0")
         library("dbcp2", "org.apache.commons:commons-dbcp2:2.12.0")
         library("pulsar-client", "org.apache.pulsar:pulsar-client:3.3.1")

         val mongo = "5.1.2"
         library("mongodb-driver-sync", "org.mongodb:mongodb-driver-sync:$mongo")
         library("mongodb-driver-coroutine", "org.mongodb:mongodb-driver-kotlin-coroutine:$mongo")

         library(
            "elasticsearch-rest-high-level-client",
            "org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.23"
         )

         library("elasticsearch-java", "co.elastic.clients:elasticsearch-java:8.14.3")

         val awssdk = "1.12.767"
         library("aws-java-sdk-dynamodb", "com.amazonaws:aws-java-sdk-dynamodb:$awssdk")
         library("aws-java-sdk-s3", "com.amazonaws:aws-java-sdk-s3:$awssdk")
         library("aws-java-sdk-sns", "com.amazonaws:aws-java-sdk-sns:$awssdk")
         library("aws-java-sdk-sqs", "com.amazonaws:aws-java-sdk-sqs:$awssdk")

         val log4j2 = "2.23.1"
         library("log4j2-api", "org.apache.logging.log4j:log4j-api:$log4j2")
         library("log4j2-core", "org.apache.logging.log4j:log4j-core:$log4j2")
         library("log4j2-slf4j2-impl", "org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2")

         val ktor = "3.2.2"
         library("ktor-client-apache5", "io.ktor:ktor-client-apache5:$ktor")
         library("ktor-server-host-common", "io.ktor:ktor-server-host-common:$ktor")
         library("ktor-server-netty", "io.ktor:ktor-server-netty:$ktor")
         library("ktor-server-test-host", "io.ktor:ktor-server-test-host:$ktor")

         val logback = "1.5.18"
         library("logback-classic", "ch.qos.logback:logback-classic:$logback")
         library("logback-core", "ch.qos.logback:logback-core:$logback")

         val jackson = "2.17.2"
         library("jackson-core", "com.fasterxml.jackson.core:jackson-core:$jackson")
         library("jackson-datatype-jsr310","com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
         library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")

         val kotest = "5.9.1"
         library("kotest-datatest", "io.kotest:kotest-framework-datatest:$kotest")
         library("kotest-junit5", "io.kotest:kotest-runner-junit5:$kotest")
         library("kotest-core", "io.kotest:kotest-assertions-core:$kotest")
         library("kotest-json", "io.kotest:kotest-assertions-json:$kotest")
         library("kotest-property", "io.kotest:kotest-property:$kotest")
         library("kotest-ktor", "io.kotest.extensions:kotest-assertions-ktor:2.0.0")

         val kotestTestContainers = "2.0.2"
         library("kotest-extensions-testcontainers", "io.kotest.extensions:kotest-extensions-testcontainers:$kotestTestContainers")
         library("kotest-extensions-testcontainers-kafka", "io.kotest.extensions:kotest-extensions-testcontainers-kafka:$kotestTestContainers")
         library(
            "kotest-extensions-testcontainers-elastic",
            "io.kotest.extensions:kotest-extensions-testcontainers-elastic:$kotestTestContainers"
         )
         library("kotest-httpstub", "io.kotest.extensions:kotest-extensions-httpstub:1.0.1")
         library("kotest-extensions-clock", "io.kotest.extensions:kotest-extensions-clock:1.0.0")

         val testContainers = "1.20.1"
         library("testcontainers", "org.testcontainers:testcontainers:$testContainers")
         library("testcontainers-postgresql", "org.testcontainers:postgresql:$testContainers")
         library("testcontainers-rabbitmq", "org.testcontainers:rabbitmq:$testContainers")
         library("testcontainers-elasticsearch", "org.testcontainers:elasticsearch:$testContainers")
         library("testcontainers-mongodb", "org.testcontainers:mongodb:$testContainers")
         library("testcontainers-kafka", "org.testcontainers:kafka:$testContainers")
         library("testcontainers-cassandra", "org.testcontainers:cassandra:$testContainers")

         val vertx = "4.5.9"
         library("vertx-core", "io.vertx:vertx-core:$vertx")
         library("vertx-web", "io.vertx:vertx-web:$vertx")
         library("vertx-kotlin", "io.vertx:vertx-lang-kotlin:$vertx")
         library("vertx-coroutines", "io.vertx:vertx-lang-kotlin-coroutines:$vertx")
         library("vertx-micrometer", "io.vertx:vertx-micrometer-metrics:$vertx")

         val cassandra = "4.19.0"
         library("cassandra", "org.apache.cassandra:java-driver-core:$cassandra")

         val mockk = "1.14.2"
         library("mockk", "io.mockk:mockk:$mockk")

         bundle(
            "testing", listOf(
               "kotest-datatest",
               "kotest-junit5",
               "kotest-core",
               "kotest-json",
               "kotest-property",
               "kotest-ktor",
               "kotest-httpstub",
               "kotest-extensions-clock",
               "kotest-extensions-testcontainers",
               "kotest-extensions-testcontainers-elastic",
               "kotest-extensions-testcontainers-kafka",
               "testcontainers",
               "testcontainers-postgresql",
               "testcontainers-kafka",
               "testcontainers-mongodb",
               "testcontainers-elasticsearch",
               "testcontainers-rabbitmq",
               "ktor-server-test-host",
            )
         )
      }
   }
}

