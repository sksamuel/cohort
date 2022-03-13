dependencies {
   implementation(project(":cohort-core"))
   implementation("co.elastic.clients:elasticsearch-java:7.17.1")
   implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
}

apply("../publish.gradle.kts")
