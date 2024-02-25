dependencies {
   implementation(projects.cohortApi)
   implementation(libs.ktor.client.apache5)
   testImplementation(projects.cohortKtor)
   testImplementation(libs.ktor.server.netty)
   testImplementation(libs.slf4j.simple)
}

apply("../publish.gradle.kts")
