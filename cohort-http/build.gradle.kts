dependencies {
   implementation(projects.cohortCore)
   implementation(libs.ktor.client.apache5)
}

apply("../publish.gradle.kts")
