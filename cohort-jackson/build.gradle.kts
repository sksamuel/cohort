dependencies {
   implementation(projects.cohortCore)
   implementation(libs.jackson.core)
   implementation(libs.jackson.module.kotlin)
}

apply("../publish.gradle.kts")
