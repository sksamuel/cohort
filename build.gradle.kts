buildscript {
   repositories {
      mavenCentral()
      maven("https://plugins.gradle.org/m2/")
   }
}

plugins {
   id("kotlin-conventions")
   id("publishing-conventions")
}

//allprojects {
//   dependencies {
//      api(rootProject.libs.coroutines.core)
//      api(rootProject.libs.coroutines.jdk8)
//      implementation(rootProject.libs.slf4j.api)
//      implementation(rootProject.libs.sksamuel.tabby)
//      testApi(rootProject.libs.bundles.testing)
//   }
//}
