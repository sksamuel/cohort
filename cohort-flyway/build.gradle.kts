dependencies {
   implementation(projects.cohortApi)
   implementation(libs.flyway.core)
}

apply("../publish.gradle.kts")
