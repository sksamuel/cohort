rootProject.name = "cohort"

plugins {
   id("de.fayard.refreshVersions") version "0.40.2"
}

refreshVersions {
   enableBuildSrcLibs()
}

refreshVersions {
   enableBuildSrcLibs()
   rejectVersionIf {
      candidate.stabilityLevel != de.fayard.refreshVersions.core.StabilityLevel.Stable
   }
}

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

         library("micrometer-core", "io.micrometer:micrometer-core:1.11.0")

         library("micrometer-core", "io.micrometer:micrometer-core:1.11.0")

         val ktor = "2.3.0"
         library("ktor-client-apache5", "io.ktor:ktor-client-apache5:$ktor")

         val kotest = "5.5.4"
         library("kotest-datatest", "io.kotest:kotest-framework-datatest:$kotest")
         library("kotest-junit5", "io.kotest:kotest-runner-junit5:$kotest")
         library("kotest-core", "io.kotest:kotest-assertions-core:$kotest")
         library("kotest-json", "io.kotest:kotest-assertions-json:$kotest")
         library("kotest-property", "io.kotest:kotest-property:$kotest")

         bundle(
            "testing", listOf(
               "kotest-datatest",
               "kotest-junit5",
               "kotest-core",
               "kotest-json",
               "kotest-property",
            )
         )
      }
   }
}
