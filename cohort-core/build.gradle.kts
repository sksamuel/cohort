dependencies {
   implementation(libs.jackson.module.kotlin)
   implementation(libs.ktor.server.host.common)
   testImplementation(libs.log4j2.slf4j2.impl)
}

apply("../publish.gradle.kts")
