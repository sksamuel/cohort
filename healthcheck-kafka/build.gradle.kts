plugins {
  kotlin("jvm")
}

dependencies {
   implementation(project(":healthcheck-core"))
   implementation("org.apache.kafka:kafka-clients:2.7.0")
}

apply("../publish.gradle.kts")
