dependencies {
   implementation(projects.cohortCore)
   implementation(libs.flyway.core)
}

apply("../publish.gradle.kts")
