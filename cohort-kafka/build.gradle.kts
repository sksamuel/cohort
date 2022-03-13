dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.kafka:kafka-clients:3.1.0")
}

apply("../publish.gradle.kts")
