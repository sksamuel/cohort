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

         val coroutines = "1.7.1"
         library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
         library("coroutines-jdk8", "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines")

         library("kotlin-logging", "io.github.oshai:kotlin-logging-jvm:4.0.0-beta-28")
         library("slf4j-api", "org.slf4j:slf4j-api:2.0.7")

         val tabby = "2.2.5"
         library("sksamuel-tabby", "com.sksamuel.tabby:tabby-fp:$tabby")

         val micrometer = "1.11.1"
         library("micrometer-core", "io.micrometer:micrometer-core:$micrometer")
         library("hikari", "com.zaxxer:HikariCP:5.0.1")

         val flyway = "9.19.4"
         library("flyway-core", "org.flywaydb:flyway-core:$flyway")

         val kakfa = "3.5.0"
         library("kafka-client", "org.apache.kafka:kafka-clients:$kakfa")

         val lettuce = "6.2.4.RELEASE"
         library("lettuce-core", "io.lettuce:lettuce-core:$lettuce")

         val liquibase = "4.22.0"
         library("liquibase-core", "org.liquibase:liquibase-core:$liquibase")

         val jedis = "4.4.3"
         library("jedis", "redis.clients:jedis:$jedis")

         library("rabbitmq", "com.rabbitmq:amqp-client:5.16.0")
         library("dbcp2", "org.apache.commons:commons-dbcp2:2.9.0")
         library("pulsar-client", "org.apache.pulsar:pulsar-client:2.10.0")

         val mongo = "4.9.1"
         library("mongodb-driver-sync", "org.mongodb:mongodb-driver-sync:$mongo")

         library(
            "elasticsearch-rest-high-level-client",
            "org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.9"
         )

         library("elasticsearch-java", "co.elastic.clients:elasticsearch-java:8.6.2")

         val awssdk = "1.12.490"
         library("aws-java-sdk-dynamodb", "com.amazonaws:aws-java-sdk-dynamodb:$awssdk")
         library("aws-java-sdk-s3", "com.amazonaws:aws-java-sdk-s3:$awssdk")
         library("aws-java-sdk-sns", "com.amazonaws:aws-java-sdk-sns:$awssdk")
         library("aws-java-sdk-sqs", "com.amazonaws:aws-java-sdk-sqs:$awssdk")

         val log4j2 = "2.20.0"
         library("log4j2-api", "org.apache.logging.log4j:log4j-api:$log4j2")
         library("log4j2-core", "org.apache.logging.log4j:log4j-core:$log4j2")
         library("log4j2-slf4j2-impl", "org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2")

         val ktor = "2.3.2"
         library("ktor-client-apache5", "io.ktor:ktor-client-apache5:$ktor")
         library("ktor-server-host-common", "io.ktor:ktor-server-host-common:$ktor")
         library("ktor-server-netty", "io.ktor:ktor-server-netty:$ktor")
         library("ktor-server-test-host", "io.ktor:ktor-server-test-host:$ktor")

         val logback = "1.4.8"
         library("logback-classic", "ch.qos.logback:logback-classic:$logback")
         library("logback-core", "ch.qos.logback:logback-core:$logback")

         val jackson = "2.15.2"
         library("jackson-core", "com.fasterxml.jackson.core:jackson-core:$jackson")
         library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")

         val kotest = "5.5.4"
         library("kotest-datatest", "io.kotest:kotest-framework-datatest:$kotest")
         library("kotest-junit5", "io.kotest:kotest-runner-junit5:$kotest")
         library("kotest-core", "io.kotest:kotest-assertions-core:$kotest")
         library("kotest-json", "io.kotest:kotest-assertions-json:$kotest")
         library("kotest-property", "io.kotest:kotest-property:$kotest")
         library("kotest-ktor", "io.kotest.extensions:kotest-assertions-ktor:2.0.0")

         val kotestTestContainers = "2.0.0"
         library("kotest-extensions-testcontainers", "io.kotest.extensions:kotest-extensions-testcontainers:$kotestTestContainers")
         library("kotest-extensions-testcontainers-kafka", "io.kotest.extensions:kotest-extensions-testcontainers-kafka:$kotestTestContainers")
         library(
            "kotest-extensions-testcontainers-elastic",
            "io.kotest.extensions:kotest-extensions-testcontainers-elastic:$kotestTestContainers"
         )
         library("kotest-httpstub", "io.kotest.extensions:kotest-extensions-httpstub:1.0.1")
         library("kotest-extensions-clock", "io.kotest.extensions:kotest-extensions-clock:1.0.0")

         val testContainers = "1.18.3"
         library("testcontainers", "org.testcontainers:testcontainers:$testContainers")
         library("testcontainers-postgresql", "org.testcontainers:postgresql:$testContainers")
         library("testcontainers-rabbitmq", "org.testcontainers:rabbitmq:$testContainers")
         library("testcontainers-elasticsearch", "org.testcontainers:elasticsearch:$testContainers")
         library("testcontainers-mongodb", "org.testcontainers:mongodb:$testContainers")
         library("testcontainers-kafka", "org.testcontainers:kafka:$testContainers")

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
