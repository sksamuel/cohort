dependencies {
   implementation(projects.cohortCore)
   api("org.elasticsearch.client:elasticsearch-rest-high-level-client:_")
   api("co.elastic.clients:elasticsearch-java:7.17.6")
   implementation("com.fasterxml.jackson.core:jackson-databind:_")
   testImplementation("org.testcontainers:elasticsearch:1.17.3")
   testImplementation("io.kotest.extensions:kotest-extensions-testcontainers-elastic:1.4.0.56-SNAPSHOT")
}

apply("../publish.gradle.kts")
