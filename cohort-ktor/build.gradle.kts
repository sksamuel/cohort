dependencies {
   api(projects.cohortCore)
   implementation("io.ktor:ktor-server-core:1.6.8")
   implementation("io.ktor:ktor-server-host-common:1.6.8")
}

apply("../publish.gradle.kts")
