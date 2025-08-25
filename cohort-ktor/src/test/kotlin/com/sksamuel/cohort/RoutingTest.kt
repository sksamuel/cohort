//package com.sksamuel.cohort
//
//import com.sksamuel.cohort.endpoints.cohort
//import io.kotest.core.spec.style.FunSpec
//import io.kotest.matchers.shouldBe
//import io.kotest.matchers.string.shouldContain
//import io.ktor.client.request.get
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.HttpStatusCode
//import io.ktor.server.routing.route
//import io.ktor.server.testing.testApplication
//
//class RoutingTest : FunSpec() {
//   init {
//      test("allow cohort to be configured in routing blocks") {
//         testApplication {
//            routing {
//               cohort {
//                  jvmInfo = true
//               }
//            }
//            client.get("/cohort/jvm").bodyAsText() shouldContain "Java Virtual Machine Specification"
//         }
//      }
//
//      test("support nested routing config") {
//         testApplication {
//            routing {
//               route("wibble") {
//                  cohort {
//                     jvmInfo = true
//                  }
//               }
//            }
//            client.get("/cohort/jvm").status shouldBe HttpStatusCode.NotFound
//            client.get("/wibble/cohort/jvm").bodyAsText() shouldContain "Java Virtual Machine Specification"
//         }
//      }
//   }
//}
