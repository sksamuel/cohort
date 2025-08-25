plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

dependencies {
   implementation(projects.cohortApi)
   implementation(libs.jackson.module.kotlin)
   api(libs.vertx.core)
   api(libs.vertx.coroutines)
   api(libs.vertx.kotlin)
   api(libs.vertx.web)
   api(libs.vertx.micrometer)
}
