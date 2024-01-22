dependencies {
   implementation(projects.cohortApi)
   api(libs.elasticsearch.rest.high.level.client)
   api(libs.elasticsearch.java)
   testImplementation(libs.testcontainers.elasticsearch)
   testImplementation(libs.kotest.extensions.testcontainers.elastic)
}

apply("../publish.gradle.kts")
