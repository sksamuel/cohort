dependencies {
   implementation(project(":cohort-core"))
   implementation("org.liquibase:liquibase-core:_")
}

apply("../publish.gradle.kts")
