dependencies {
   implementation(projects.cohortCore)
   implementation(libs.pulsar.client)
}

apply("../publish.gradle.kts")
