plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   api(libs.aws.java.sdk.sqs)
}
