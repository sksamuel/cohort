dependencies {
   implementation(projects.cohortCore)
   implementation(libs.ktor.client.apache5)
   testImplementation(libs.ktor.server.netty)
   testImplementation(libs.slf4j.simple)
}

apply("../publish.gradle.kts")
