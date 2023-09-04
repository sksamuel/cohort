dependencies {
   implementation(projects.cohortCore)
   api(libs.jedis)
}

apply("../publish.gradle.kts")
