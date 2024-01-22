dependencies {
   implementation(projects.cohortApi)
   implementation(libs.ktor.client.apache5)
   testImplementation(projects.cohortCore)
   testImplementation(libs.ktor.server.netty)
   testImplementation(libs.slf4j.simple)
}

apply("../publish.gradle.kts")
