dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.kafka:kafka-clients:_")
}

apply("../publish.gradle.kts")
