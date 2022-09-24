dependencies {
   implementation(projects.cohortCore)
   implementation("org.apache.pulsar:pulsar-client:_")
}

apply("../publish.gradle.kts")
