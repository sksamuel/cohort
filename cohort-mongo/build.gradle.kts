dependencies {
   implementation(projects.cohortCore)
   api("org.mongodb:mongodb-driver-sync:_")
   testImplementation("org.testcontainers:mongodb:_")
   testImplementation(Testing.kotestExtensions.testContainers)
}

apply("../publish.gradle.kts")
