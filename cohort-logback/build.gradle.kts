dependencies {
   implementation(projects.cohortCore)
   implementation(libs.logback.core)
   implementation(libs.logback.classic)
}
apply("../publish.gradle.kts")
