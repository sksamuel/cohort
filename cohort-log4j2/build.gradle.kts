dependencies {
   implementation(projects.cohortCore)
   implementation(libs.log4j2.api)
   implementation(libs.log4j2.core)
}

apply("../publish.gradle.kts")
