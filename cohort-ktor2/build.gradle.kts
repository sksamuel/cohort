dependencies {
   api(project(":cohort-core"))
   implementation("io.ktor:ktor-server-core:2.0.0")
   implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
}

apply("../publish.gradle.kts")
