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
