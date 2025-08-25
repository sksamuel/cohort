plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   testImplementation("com.h2database:h2:2.3.230")
   testImplementation(libs.hikari)
}
