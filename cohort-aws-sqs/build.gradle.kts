dependencies {
   implementation(projects.cohortCore)
   api(libs.aws.java.sdk.sqs)
}

apply("../publish.gradle.kts")
