dependencies {
   implementation(projects.cohortCore)
   api("io.lettuce:lettuce-core:6.2.0.RELEASE")
}

apply("../publish.gradle.kts")
