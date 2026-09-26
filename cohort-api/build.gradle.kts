plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   testImplementation("com.h2database:h2:2.5.252")
   testImplementation(libs.hikari)
}
