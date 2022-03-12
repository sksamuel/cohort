object Libs {

  const val kotlinVersion = "1.6.10"
  const val org = "com.sksamuel.healthcheck"

  object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.1.1"
  }

  object Kotest {
    private const val version = "5.1.0"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }
}
