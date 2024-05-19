dependencies {
   implementation(projects.cohortApi)
   compileOnly(libs.mongodb.driver.sync)
   compileOnly(libs.mongodb.driver.coroutine)
   testImplementation(libs.testcontainers.mongodb)
   testImplementation(libs.mongodb.driver.sync)
   testImplementation(libs.mongodb.driver.coroutine)
}

apply("../publish.gradle.kts")
