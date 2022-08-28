dependencies {
   implementation(projects.cohortCore)
   implementation("org.flywaydb:flyway-core:_")
}

apply("../publish.gradle.kts")
