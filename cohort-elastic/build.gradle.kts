dependencies {
   implementation(projects.cohortCore)
   api("org.elasticsearch.client:elasticsearch-rest-high-level-client:_")
   api("co.elastic.clients:elasticsearch-java:_")
   implementation("com.fasterxml.jackson.core:jackson-databind:_")
   testImplementation("org.testcontainers:elasticsearch:_")
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers-elastic:_")
}

apply("../publish.gradle.kts")
