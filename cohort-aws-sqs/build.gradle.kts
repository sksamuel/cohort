dependencies {
   implementation(projects.cohortApi)
   api(libs.aws.java.sdk.sqs)
}

apply("../publish.gradle.kts")
