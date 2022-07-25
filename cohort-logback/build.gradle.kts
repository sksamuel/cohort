dependencies {
   implementation(project(":cohort-core"))
   implementation("ch.qos.logback:logback-classic:_")
   implementation("ch.qos.logback:logback-core:_")
}
apply("../publish.gradle.kts")
