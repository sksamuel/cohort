dependencies {
   api(projects.cohortApi)
   implementation(libs.jackson.module.kotlin)
   implementation(libs.ktor.server.host.common)
   testImplementation(libs.log4j2.slf4j2.impl)
   testImplementation("com.h2database:h2:2.1.214")
}

apply("../publish.gradle.kts")
