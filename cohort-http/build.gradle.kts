dependencies {
   implementation(project(":cohort-core"))
   implementation("io.ktor:ktor-client-cio:1.6.8")
}

apply("../publish.gradle.kts")
