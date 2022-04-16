dependencies {
   implementation(project(":cohort-core"))
   api("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.10.2")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
}

apply("../publish.gradle.kts")
