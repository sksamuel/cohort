dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.logging.log4j:log4j-api:2.17.2")
   implementation("org.apache.logging.log4j:log4j-core:2.17.2")
}
apply("../publish.gradle.kts")
