dependencies {
   implementation(project(":cohort-core"))
   implementation("io.ktor:ktor-server-core:1.6.7")
   implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
}

apply("../publish.gradle.kts")
