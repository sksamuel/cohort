package com.sksamuel.cohort.jvm

import java.lang.management.ManagementFactory

// JVM args commonly carry secrets passed via `-D` flags
// (`-Djavax.net.ssl.keyStorePassword=...`, `-Daws.secretAccessKey=...`, custom `-Dapi.token=...`)
// or via `-javaagent` license/key params. Without redaction `/cohort/jvm` leaks them.
// Mirrors the sysprops redaction added previously.
private val SENSITIVE_PATTERNS = listOf(
  "password", "passwd", "secret", "token", "credential", "apikey", "api_key", "private",
  "license_key", "license-key",
)

private fun isSensitive(s: String): Boolean {
  val lower = s.lowercase()
  return SENSITIVE_PATTERNS.any { lower.contains(it) }
}

internal fun redactVmArg(arg: String): String {
  // Redact -DkeyName=value when the key matches the heuristic.
  if (arg.startsWith("-D")) {
    val eq = arg.indexOf('=')
    if (eq > 2) {
      val key = arg.substring(2, eq)
      if (isSensitive(key)) return "${arg.substring(0, eq + 1)}***REDACTED***"
    }
  }
  // Redact `-javaagent:.../foo.jar=license_key=...` style args.
  if (isSensitive(arg)) {
    // Best-effort: keep everything before the first '=' so the arg shape stays visible.
    val eq = arg.indexOf('=')
    if (eq > 0) return "${arg.substring(0, eq + 1)}***REDACTED***"
    return "***REDACTED***"
  }
  return arg
}

fun getJvmDetails(): Result<Jvm> = runCatching {
  val bean = ManagementFactory.getRuntimeMXBean()
  Jvm(
    name = bean.name,
    pid = bean.pid,
    vmOptions = bean.inputArguments.map(::redactVmArg),
    classPath = bean.classPath,
    specName = bean.specName,
    specVendor = bean.specVendor,
    specVersion = bean.specVersion,
    vmName = bean.vmName,
    vmVendor = bean.vmVendor,
    vmVersion = bean.vmVersion,
    startTime = bean.startTime,
    uptime = bean.uptime,
  )
}

data class Jvm(
  val name: String,
  val pid: Long,
  val vmOptions: List<String>,
  val classPath: String,
  val specName: String,
  val specVendor: String,
  val specVersion: String,
  val vmName: String,
  val vmVendor: String,
  val vmVersion: String,
  val startTime: Long,
  val uptime: Long
)
