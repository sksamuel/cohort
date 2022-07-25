dependencies {
   implementation(project(":cohort-core"))
   implementation("com.zaxxer:HikariCP:_")
}
apply("../publish.gradle.kts")
