dependencies {
   implementation(projects.cohortCore)
   api("com.amazonaws:aws-java-sdk-sns:_")
}

apply("../publish.gradle.kts")
