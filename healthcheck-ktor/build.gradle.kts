plugins {
   kotlin("jvm")
}

dependencies {
   implementation(project(":healthcheck-core"))
   implementation("io.ktor:ktor-server-core:1.4.3")
   implementation("com.fasterxml.jackson.core:jackson-core:2.12.1")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")

}

apply("../publish.gradle.kts")
