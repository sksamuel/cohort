rootProject.name = "cohort"

include(
   "cohort-aws-dynamo",
   "cohort-aws-s3",
   "cohort-aws-sqs",
   "cohort-aws-sns",
   "cohort-core",
   "cohort-dbcp",
   "cohort-elastic",
   "cohort-flyway",
   "cohort-hikari",
   "cohort-http",
   "cohort-jackson",
   "cohort-kafka",
   "cohort-lettuce",
   "cohort-liquibase",
   "cohort-log4j2",
   "cohort-logback",
   "cohort-mongo",
   "cohort-pulsar",
   "cohort-rabbit",
   "cohort-redis",
   "cohort-micrometer",
)

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
   versionCatalogs {
      create("libs") {

         val coroutines = "1.6.4"
         library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
         library("coroutines-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines")

         library("kotlin-logging", "io.github.oshai:kotlin-logging-jvm:4.0.0-beta-28")
         library("slf4j-api", "org.slf4j:slf4j-api:2.0.7")

         library("sksamuel-tabby", "com.sksamuel.tabby:tabby-fp:2.2.3")

         library("micrometer-core", "io.micrometer:micrometer-core:1.11.0")
         library("hikari", "com.zaxxer:HikariCP:5.0.1")
         library("flyway-core", "org.flywaydb:flyway-core:9.17.0")
         library("kafka-client", "org.apache.kafka:kafka-clients:3.4.0")
         library("lettuce-core", "io.lettuce:lettuce-core:6.2.3.RELEASE")
         library("liquibase-core", "org.liquibase:liquibase-core:4.21.1")
         library("jedis", "redis.clients:jedis:4.4.0")
         library("rabbitmq", "com.rabbitmq:amqp-client:5.16.0")
         library("dbcp2", "org.apache.commons:commons-dbcp2:2.9.0")
         library("pulsar-client", "org.apache.pulsar:pulsar-client:2.10.0")
         library("mongodb-driver-sync", "org.mongodb:mongodb-driver-sync:4.9.0")

         library(
            "elasticsearch-rest-high-level-client",
            "org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.9"
         )

         library("elasticsearch-java", "co.elastic.clients:elasticsearch-java:8.6.2")

         val awssdk = "1.12.466"
         library("aws-java-sdk-dynamodb", "com.amazonaws:aws-java-sdk-dynamodb:$awssdk")
         library("aws-java-sdk-s3", "com.amazonaws:aws-java-sdk-s3:$awssdk")
         library("aws-java-sdk-sns", "com.amazonaws:aws-java-sdk-sns:$awssdk")
         library("aws-java-sdk-sqs", "com.amazonaws:aws-java-sdk-sqs:$awssdk")

         val log4j2 = "2.20.0"
         library("log4j2-api", "org.apache.logging.log4j:log4j-api:$log4j2")
         library("log4j2-core", "org.apache.logging.log4j:log4j-core:$log4j2")
         library("log4j2-slf4j2-impl", "org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2")

         val ktor = "2.3.0"
         library("ktor-client-apache5", "io.ktor:ktor-client-apache5:$ktor")
         library("ktor-server-host-common", "io.ktor:ktor-server-host-common:$ktor")
         library("ktor-server-netty", "io.ktor:ktor-server-netty:$ktor")

         val logback = "1.4.6"
         library("logback-classic", "ch.qos.logback:logback-classic:$logback")
         library("logback-core", "ch.qos.logback:logback-core:$logback")

         val jackson = "2.14.2"
         library("jackson-core", "com.fasterxml.jackson.core:jackson-core:$jackson")
         library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")

         val kotest = "5.5.4"
         library("kotest-datatest", "io.kotest:kotest-framework-datatest:$kotest")
         library("kotest-junit5", "io.kotest:kotest-runner-junit5:$kotest")
         library("kotest-core", "io.kotest:kotest-assertions-core:$kotest")
         library("kotest-json", "io.kotest:kotest-assertions-json:$kotest")
         library("kotest-property", "io.kotest:kotest-property:$kotest")
         library("kotest-ktor", "io.kotest.extensions:kotest-assertions-ktor:2.0.0")
         library("kotest-testcontainers", "io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
         library(
            "kotest-extensions-testcontainers-elastic",
            "io.kotest.extensions:kotest-extensions-testcontainers-elastic:1.4.0.56-SNAPSHOT"
         )
         library("kotest-httpstub", "io.kotest.extensions:kotest-extensions-httpstub:1.0.1")
         library("kotest-extensions-clock", "io.kotest.extensions:kotest-extensions-clock:1.0.0")

         val testContainers = "1.18.0"
         library("testcontainers", "org.testcontainers:testcontainers:$testContainers")
         library("testcontainers-postgresql", "org.testcontainers:postgresql:$testContainers")
         library("testcontainers-rabbitmq", "org.testcontainers:rabbitmq:$testContainers")
         library("testcontainers-elasticsearch", "org.testcontainers:elasticsearch:$testContainers")
         library("testcontainers-mongodb", "org.testcontainers:mongodb:$testContainers")


         bundle(
            "testing", listOf(
               "kotest-datatest",
               "kotest-junit5",
               "kotest-core",
               "kotest-json",
               "kotest-property",
               "kotest-ktor",
               "kotest-testcontainers",
               "kotest-httpstub",
               "testcontainers",
               "testcontainers-postgresql",
               "kotest-extensions-clock",
            )
         )
      }
   }
}
