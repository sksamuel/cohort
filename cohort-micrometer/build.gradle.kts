dependencies {
   implementation(projects.cohortCore)
   implementation("io.micrometer:micrometer-core:_")
}

apply("../publish.gradle.kts")
