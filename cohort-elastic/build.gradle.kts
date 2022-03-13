dependencies {
   implementation(project(":cohort-core"))
   implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.16.3")
}

apply("../publish.gradle.kts")
