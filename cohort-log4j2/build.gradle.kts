dependencies {
   implementation(projects.cohortCore)
   implementation("org.apache.logging.log4j:log4j-api:_")
   implementation("org.apache.logging.log4j:log4j-core:_")
}

apply("../publish.gradle.kts")
