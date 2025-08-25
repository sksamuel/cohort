plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   implementation(libs.logback.core)
   implementation(libs.logback.classic)
}
