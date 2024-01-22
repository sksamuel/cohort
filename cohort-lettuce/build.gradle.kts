dependencies {
   implementation(projects.cohortApi)
   api(libs.lettuce.core)
}

apply("../publish.gradle.kts")
