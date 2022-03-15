dependencies {
   implementation(project(":cohort-core"))
   implementation("ch.qos.logback:logback-classic:1.2.11")
   implementation("ch.qos.logback:logback-core:1.2.11")
}
apply("../publish.gradle.kts")
