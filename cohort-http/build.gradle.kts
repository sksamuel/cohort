dependencies {
   implementation(projects.cohortCore)
   implementation(libs.ktor.client.apache5)
   testImplementation(libs.ktor.server.netty)
}

apply("../publish.gradle.kts")
