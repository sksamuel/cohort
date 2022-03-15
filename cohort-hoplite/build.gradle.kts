dependencies {
   implementation(project(":cohort-core"))
   api("com.sksamuel.hoplite:hoplite-core:1.4.16")
}

apply("../publish.gradle.kts")
