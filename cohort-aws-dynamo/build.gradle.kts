dependencies {
   implementation(projects.cohortCore)
   api("com.amazonaws:aws-java-sdk-dynamodb:_")
}

apply("../publish.gradle.kts")
