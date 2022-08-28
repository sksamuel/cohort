dependencies {
   implementation(projects.cohortCore)
   implementation("org.liquibase:liquibase-core:_")
}

apply("../publish.gradle.kts")
