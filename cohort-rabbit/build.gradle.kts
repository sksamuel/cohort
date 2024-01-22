dependencies {
   implementation(projects.cohortApi)
   api(libs.rabbitmq)
   testImplementation(libs.testcontainers.rabbitmq)
}

apply("../publish.gradle.kts")
