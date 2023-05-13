dependencies {
   implementation(projects.cohortCore)
   api(libs.aws.java.sdk.dynamodb)
}

apply("../publish.gradle.kts")
