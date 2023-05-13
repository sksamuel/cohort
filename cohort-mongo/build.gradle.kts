dependencies {
   implementation(projects.cohortCore)
   api(libs.mongodb.driver.sync)
   testImplementation(libs.testcontainers.mongodb)
}

apply("../publish.gradle.kts")
