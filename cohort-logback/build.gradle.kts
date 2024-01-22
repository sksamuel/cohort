dependencies {
   implementation(projects.cohortApi)
   implementation(libs.logback.core)
   implementation(libs.logback.classic)
}
apply("../publish.gradle.kts")
