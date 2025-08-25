plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   implementation(libs.hikari)
   testImplementation("com.h2database:h2:2.3.230")
   testImplementation(libs.log4j2.slf4j2.impl)
}
