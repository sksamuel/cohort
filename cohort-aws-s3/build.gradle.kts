dependencies {
   implementation(projects.cohortCore)
   implementation("com.amazonaws:aws-java-sdk-s3:_")
}

apply("../publish.gradle.kts")
