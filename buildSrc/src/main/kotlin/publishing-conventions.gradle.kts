plugins {
   id("com.vanniktech.maven.publish")
}

group = "com.sksamuel.cohort"
version = Ci.version

mavenPublishing {
   publishToMavenCentral(automaticRelease = true)
   signAllPublications()
   pom {
      name.set("cohort")
      description.set("Ktor and Vertx Actuator and Monitoring")
      url.set("https://www.github.com/sksamuel/cohort")

      scm {
         connection.set("scm:git:https://www.github.com/sksamuel/cohort/")
         developerConnection.set("scm:git:https://github.com/sksamuel/")
         url.set("https://www.github.com/sksamuel/cohort/")
      }

      licenses {
         license {
            name.set("The Apache 2.0 License")
            url.set("https://opensource.org/licenses/Apache-2.0")
         }
      }

      developers {
         developer {
            id.set("sksamuel")
            name.set("Stephen Samuel")
            email.set("sam@sksamuel.com")
         }
      }
   }
}
