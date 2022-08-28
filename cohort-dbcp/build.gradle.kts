dependencies {
   implementation(projects.cohortCore)
   implementation("org.apache.commons:commons-dbcp2:_")
}

apply("../publish.gradle.kts")
