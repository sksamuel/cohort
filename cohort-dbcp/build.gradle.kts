dependencies {
   implementation(projects.cohortApi)
   implementation(libs.dbcp2)
}

apply("../publish.gradle.kts")
