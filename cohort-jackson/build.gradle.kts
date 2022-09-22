dependencies {
   implementation(projects.cohortCore)
   implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
}

apply("../publish.gradle.kts")
