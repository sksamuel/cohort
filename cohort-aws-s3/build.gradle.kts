dependencies {
   implementation(projects.cohortCore)
   api(libs.aws.java.sdk.s3)
}

apply("../publish.gradle.kts")
