dependencies {
   implementation(projects.cohortCore)
   implementation(Ktor.client.apache)
   implementation("io.github.microutils:kotlin-logging:3.0.5")
}

apply("../publish.gradle.kts")
