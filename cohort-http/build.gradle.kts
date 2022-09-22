dependencies {
   implementation(projects.cohortCore)
   implementation(Ktor.client.apache)
}

apply("../publish.gradle.kts")
