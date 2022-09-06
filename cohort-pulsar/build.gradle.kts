dependencies {
   implementation(projects.cohortCore)
   implementation("org.apache.pulsar:pulsar-client:2.10.1")
}

apply("../publish.gradle.kts")
