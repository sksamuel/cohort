dependencies {
   implementation(projects.cohortCore)
   api("com.rabbitmq:amqp-client:_")
   testImplementation("org.testcontainers:rabbitmq:_")
   testImplementation(Testing.kotestExtensions.testContainers)
}

apply("../publish.gradle.kts")
