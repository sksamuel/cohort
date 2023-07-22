dependencies {
   implementation(projects.cohortCore)
   implementation(libs.hikari)
   testImplementation("com.h2database:h2:2.1.214")
   testImplementation(libs.log4j2.slf4j2.impl)
}

apply("../publish.gradle.kts")
