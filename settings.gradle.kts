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

include("cohort-core")
include("cohort-dbcp")
include("cohort-elastic")
include("cohort-flyway")
include("cohort-hikari")
include("cohort-http")
include("cohort-kafka")
include("cohort-ktor")
include("cohort-ktor2")
include("cohort-liquibase")
include("cohort-log4j2")
include("cohort-logback")
include("cohort-redis")
include("cohort-aws-s3")
include("cohort-aws-sqs")
