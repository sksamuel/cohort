dependencies {
   implementation(projects.cohortApi)
   implementation(libs.kafka.client)
}

apply("../publish.gradle.kts")
