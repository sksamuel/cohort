dependencies {
   api(projects.cohortApi)
   implementation(libs.jackson.module.kotlin)
   implementation(libs.ktor.server.host.common)
   implementation(libs.ktor.client.apache5)
   testImplementation(libs.ktor.server.netty)
   testImplementation(libs.slf4j.simple)
   testImplementation(libs.log4j2.slf4j2.impl)
   testImplementation("com.h2database:h2:2.3.230")
}

apply("../publish.gradle.kts")
