dependencies {
   implementation(projects.cohortCore)
   api(libs.lettuce.core)
}

apply("../publish.gradle.kts")
