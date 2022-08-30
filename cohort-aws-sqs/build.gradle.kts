dependencies {
   implementation(projects.cohortCore)
   api("com.amazonaws:aws-java-sdk-sqs:_")
}

apply("../publish.gradle.kts")
