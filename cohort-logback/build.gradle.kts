dependencies {
   implementation(projects.cohortCore)
   implementation("ch.qos.logback:logback-classic:_")
   implementation("ch.qos.logback:logback-core:_")
}
apply("../publish.gradle.kts")
