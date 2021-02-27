object Libs {

  const val kotlinVersion = "1.4.31"
  const val org = "com.sksamuel.healthcheck"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.1.1"
  }

  object Aws {
    private const val version = "1.11.954"
    const val core = "com.amazonaws:aws-java-sdk-core:$version"
    const val ssm = "com.amazonaws:aws-java-sdk-ssm:$version"
  }

  object Jackson {
    const val core = "com.fasterxml.jackson.core:jackson-core:2.12.1"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.12.1"
  }

  object Kotest {
    private const val version = "4.4.1"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }
}
