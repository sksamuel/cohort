rootProject.name = "cohort"

plugins {
   id("de.fayard.refreshVersions") version "0.40.2"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
   "cohort-ktor",
   "cohort-ktor2",
   "cohort-liquibase",
   "cohort-log4j2",
   "cohort-logback",
   "cohort-mongo",
   "cohort-pulsar",
   "cohort-rabbit",
   "cohort-redis",
   "cohort-micrometer",
)
