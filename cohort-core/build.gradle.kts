dependencies {
   implementation("io.github.microutils:kotlin-logging:2.1.21")
   implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
}

apply("../publish.gradle.kts")
