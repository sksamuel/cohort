plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   api(libs.rabbitmq)
   testImplementation(libs.testcontainers.rabbitmq)
}
