plugins {
  kotlin("jvm")
}

dependencies {
   implementation(project(":healthcheck-core"))
   implementation("io.ktor:ktor-server-core:1.4.3")
}

apply("../publish.gradle.kts")
