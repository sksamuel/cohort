dependencies {
   implementation(projects.cohortCore)
   implementation("com.rabbitmq:amqp-client:5.14.2")
   testImplementation("org.testcontainers:rabbitmq:1.17.3")
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
}

apply("../publish.gradle.kts")
