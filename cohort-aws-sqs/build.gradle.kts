dependencies {
   implementation(projects.cohortCore)
   implementation("com.amazonaws:aws-java-sdk-sqs:_")
}

apply("../publish.gradle.kts")
