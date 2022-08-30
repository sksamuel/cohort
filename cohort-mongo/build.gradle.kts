dependencies {
   implementation(projects.cohortCore)
   implementation("org.mongodb:mongodb-driver-sync:4.7.1")
   testImplementation("org.testcontainers:mongodb:1.17.3")
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.3.4")
}

apply("../publish.gradle.kts")
