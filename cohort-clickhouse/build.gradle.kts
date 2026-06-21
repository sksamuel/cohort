plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   api(libs.clickhouse.client)
   testImplementation(libs.testcontainers)
}

// The ClickHouse client pulls in the maintained at.yawk.lz4:lz4-java fork, while the kotest
// testcontainers extension transitively pulls the original org.lz4:lz4-java (via kafka-clients).
// Both declare the same lz4-java capability, so the test classpath fails to resolve unless we
// pick one. The fork is a drop-in continuation of the original, so select the highest version.
configurations.testRuntimeClasspath {
   resolutionStrategy.capabilitiesResolution.withCapability("org.lz4:lz4-java") {
      selectHighestVersion()
   }
}
