dependencies {
   implementation(projects.cohortCore)
   api(libs.elasticsearch.rest.high.level.client)
//   api("org.elasticsearch.client::7.17.9")
//   api("co.elastic.clients:elasticsearch-java:8.6.2")
//   implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
   testImplementation(libs.testcontainers.elasticsearch)
   testImplementation(libs.kotest.extensions.testcontainers.elastic)
}

apply("../publish.gradle.kts")
