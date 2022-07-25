dependencies {
   implementation(project(":cohort-core"))
   implementation("org.flywaydb:flyway-core:_")
}

apply("../publish.gradle.kts")
