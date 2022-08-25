dependencies {
   implementation(project(":cohort-core"))
   implementation("com.amazonaws:aws-java-sdk-sqs:_")
}

apply("../publish.gradle.kts")
