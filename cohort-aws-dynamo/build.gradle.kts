dependencies {
   implementation(projects.cohortApi)
   api(libs.aws.java.sdk.dynamodb)
}

apply("../publish.gradle.kts")
