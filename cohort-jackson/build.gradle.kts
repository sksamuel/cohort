dependencies {
   implementation(projects.cohortApi)
   implementation(libs.jackson.core)
   implementation(libs.jackson.module.kotlin)
}

apply("../publish.gradle.kts")
