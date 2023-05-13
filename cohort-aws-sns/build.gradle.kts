dependencies {
   implementation(projects.cohortCore)
   api(libs.aws.java.sdk.sns)
}

apply("../publish.gradle.kts")
