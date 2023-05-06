dependencies {
   implementation("io.github.microutils:kotlin-logging:_")
   implementation("com.fasterxml.jackson.core:jackson-core:_")
   implementation("com.fasterxml.jackson.core:jackson-databind:_")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
   implementation("io.ktor:ktor-server-host-common:_")
}

apply("../publish.gradle.kts")
