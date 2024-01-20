dependencies {
   implementation(projects.cohortApi)
   implementation(libs.liquibase.core)
}

apply("../publish.gradle.kts")
