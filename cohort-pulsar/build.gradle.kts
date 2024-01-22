dependencies {
   implementation(projects.cohortApi)
   implementation(libs.pulsar.client)
}

apply("../publish.gradle.kts")
