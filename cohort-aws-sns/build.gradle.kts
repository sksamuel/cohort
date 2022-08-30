dependencies {
   implementation(projects.cohortCore)
   implementation("com.amazonaws:aws-java-sdk-sns:_")
}

apply("../publish.gradle.kts")
