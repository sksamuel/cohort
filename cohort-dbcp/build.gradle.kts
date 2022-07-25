dependencies {
   implementation(project(":cohort-core"))
   implementation("org.apache.commons:commons-dbcp2:_")
}

apply("../publish.gradle.kts")
