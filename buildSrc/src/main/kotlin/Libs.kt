object Libs {

  const val kotlinVersion = "_"
  const val org = "com.sksamuel.cohort"

  object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:_"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:_"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:_"
    const val coroutinesJdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:_"
  }

  object Kotest {
    private const val version = "_"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:_"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:_"
  }

  object Tabby {
    private const val version = "_"
    const val fp = "com.sksamuel.tabby:tabby-fp:_"
  }
}
