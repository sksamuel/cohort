plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   testImplementation("com.h2database:h2:2.4.240")
   testImplementation(libs.hikari)
}
