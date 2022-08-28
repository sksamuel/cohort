dependencies {
   implementation(projects.cohortCore)
   implementation(Ktor.client.cio)
}

apply("../publish.gradle.kts")
