dependencies {
   implementation(projects.cohortCore)
   implementation(libs.liquibase.core)
}

apply("../publish.gradle.kts")
