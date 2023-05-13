dependencies {
   implementation(projects.cohortCore)
   implementation(libs.dbcp2)
}

apply("../publish.gradle.kts")
