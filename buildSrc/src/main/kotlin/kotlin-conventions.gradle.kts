import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
   `java-library`
   kotlin("jvm")
}

java {
   sourceCompatibility = JavaVersion.VERSION_11
   targetCompatibility = JavaVersion.VERSION_11
}

group = "com.sksamuel.cohort"
version = Ci.version

dependencies {
   implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
   testImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
   testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
   testImplementation("io.kotest:kotest-assertions-core:5.9.1")
   testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
}

kotlin {
   jvmToolchain(11)
   compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      apiVersion.set(KotlinVersion.KOTLIN_2_1)
      languageVersion.set(KotlinVersion.KOTLIN_2_1)
   }
}

tasks.test {
   useJUnitPlatform()
   filter {
      isFailOnNoMatchingTests = false
   }
   testLogging {
      showExceptions = true
      showStandardStreams = true
      events = setOf(
         TestLogEvent.FAILED,
         TestLogEvent.PASSED
      )
      exceptionFormat = TestExceptionFormat.FULL
   }
}
