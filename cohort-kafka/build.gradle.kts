plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   implementation(libs.kafka.client)
}
