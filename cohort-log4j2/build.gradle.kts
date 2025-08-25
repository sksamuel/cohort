plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   implementation(libs.log4j2.api)
   implementation(libs.log4j2.core)
}
