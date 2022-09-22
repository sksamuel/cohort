dependencies {
   implementation(projects.cohortCore)
   implementation(Ktor.client.apache)
   implementation("io.github.microutils:kotlin-logging:2.1.23")
}

apply("../publish.gradle.kts")
