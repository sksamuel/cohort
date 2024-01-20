dependencies {
   implementation(projects.cohortApi)
   implementation(libs.micrometer.core)
}

apply("../publish.gradle.kts")
