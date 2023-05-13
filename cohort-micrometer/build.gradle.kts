dependencies {
   implementation(projects.cohortCore)
   implementation(libs.micrometer.core)
}

apply("../publish.gradle.kts")
