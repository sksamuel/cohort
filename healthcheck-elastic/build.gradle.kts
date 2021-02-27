plugins {
  kotlin("jvm")
}

dependencies {
   implementation(project(":healthcheck-core"))
   implementation("org.elasticsearch.client:elasticsearch-rest-client:7.11.1")
}

apply("../publish.gradle.kts")
