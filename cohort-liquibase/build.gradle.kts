dependencies {
   implementation(project(":cohort-core"))
   implementation("org.liquibase:liquibase-core:4.8.0")
}

apply("../publish.gradle.kts")
