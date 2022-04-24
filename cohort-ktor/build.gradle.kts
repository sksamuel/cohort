dependencies {
   api(project(":cohort-core"))
   implementation("io.ktor:ktor-server-core:1.6.8")
}

apply("../publish.gradle.kts")
