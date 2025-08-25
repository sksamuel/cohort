plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   api(libs.cassandra)
   testImplementation(libs.testcontainers.cassandra)
   testImplementation(libs.slf4j.simple)
   testImplementation(libs.mockk)
}
