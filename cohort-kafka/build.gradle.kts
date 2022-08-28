dependencies {
   implementation(projects.cohortCore)
   implementation("org.apache.kafka:kafka-clients:_")
}

apply("../publish.gradle.kts")
