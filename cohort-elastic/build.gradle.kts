plugins {
  kotlin("jvm")
}

dependencies {
   implementation(project(":cohort-core"))
   implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.11.1")
}

apply("../publish.gradle.kts")
