dependencies {
   api(projects.cohortCore)
   implementation(Ktor.server.core)
   implementation("io.ktor:ktor-server-host-common:_")
}

apply("../publish.gradle.kts")
