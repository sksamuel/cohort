dependencies {
   implementation(projects.cohortCore)
   implementation("com.fasterxml.jackson.core:jackson-core:_")
   implementation("com.fasterxml.jackson.module:jackson-module-kotlin:_")
}

apply("../publish.gradle.kts")
