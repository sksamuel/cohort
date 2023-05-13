dependencies {
   implementation(projects.cohortCore)
   implementation(libs.kafka.client)
}

apply("../publish.gradle.kts")
