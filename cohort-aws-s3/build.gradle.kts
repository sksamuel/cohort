dependencies {
   implementation(project(":cohort-core"))
   implementation("com.amazonaws:aws-java-sdk-s3:_")
}

apply("../publish.gradle.kts")
