dependencies {
   implementation(project(":cohort-core"))
   implementation("org.flywaydb:flyway-core:8.5.3")
}
apply("../publish.gradle.kts")
