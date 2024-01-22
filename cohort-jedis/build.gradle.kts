dependencies {
   implementation(projects.cohortApi)
   api(libs.jedis)
}

apply("../publish.gradle.kts")
