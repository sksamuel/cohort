dependencies {
   implementation(projects.cohortApi)
   api(libs.aws.java.sdk.s3)
}

apply("../publish.gradle.kts")
