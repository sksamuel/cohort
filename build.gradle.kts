import org.gradle.api.tasks.testing.logging.TestExceptionFormat

buildscript {
   repositories {
      mavenCentral()
      maven {
         url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
      }
      maven {
         url = uri("https://plugins.gradle.org/m2/")
      }
   }
}

plugins {
   signing
   `maven-publish`
   kotlin("jvm").version("1.9.25")
}

allprojects {
   apply(plugin = "org.jetbrains.kotlin.jvm")

   kotlin {
      jvmToolchain(11)
   }

   repositories {
      mavenLocal()
      mavenCentral()
      maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
      maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
   }

   group = "com.sksamuel.cohort"
   version = Ci.version

   dependencies {
      api(rootProject.libs.coroutines.core)
      api(rootProject.libs.coroutines.jdk8)
      implementation(rootProject.libs.slf4j.api)
      implementation(rootProject.libs.sksamuel.tabby)
      testApi(rootProject.libs.bundles.testing)
   }

   tasks.named<Test>("test") {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
         showStandardStreams = true
         exceptionFormat = TestExceptionFormat.FULL
      }
   }
}
