dependencies {
   implementation(projects.cohortApi)
   compileOnly(libs.mongodb.driver.sync)
   testImplementation(libs.testcontainers.mongodb)
   testImplementation(libs.mongodb.driver.sync)
}

apply("../publish.gradle.kts")
