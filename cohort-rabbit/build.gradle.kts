dependencies {
   implementation(projects.cohortCore)
   api(libs.rabbitmq)
   testImplementation(libs.testcontainers.rabbitmq)
}

apply("../publish.gradle.kts")
