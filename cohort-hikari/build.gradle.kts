dependencies {
   implementation(projects.cohortCore)
   implementation(libs.hikari)
   implementation("io.arrow-kt:arrow-core:1.1.3")
   implementation("com.h2database:h2:2.1.214")
}

apply("../publish.gradle.kts")
