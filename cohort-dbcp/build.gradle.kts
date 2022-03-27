dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.commons:commons-dbcp2:2.9.0")
}

apply("../publish.gradle.kts")
