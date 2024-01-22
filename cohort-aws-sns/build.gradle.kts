dependencies {
   implementation(projects.cohortApi)
   api(libs.aws.java.sdk.sns)
}

apply("../publish.gradle.kts")
