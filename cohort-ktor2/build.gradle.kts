dependencies {
   api(project(":cohort-core"))
   implementation("io.ktor:ktor-server-core:2.0.0")
}

apply("../publish.gradle.kts")
