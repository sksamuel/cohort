dependencies {
   implementation(project(":cohort-core"))
   implementation(Ktor.client.cio)
}

apply("../publish.gradle.kts")
