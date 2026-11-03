package com.sksamuel.cohort.system

// Keys whose value should be redacted when exposed via the /cohort/sysprops endpoint.
// System properties commonly carry credentials passed via -D flags (e.g.
// -Djavax.net.ssl.keyStorePassword=..., -Daws.secretAccessKey=..., custom -Dapi.token=...).
// Without redaction, /cohort/sysprops leaks them to anyone with access to the endpoint.
private val SENSITIVE_KEY_PATTERNS = listOf(
  "password", "passwd", "secret", "token", "credential", "apikey", "api_key", "private",
)

private fun isSensitive(key: String): Boolean {
  val lower = key.lowercase()
  return SENSITIVE_KEY_PATTERNS.any { lower.contains(it) }
}

fun getSysProps(): Result<SysProps> = runCatching {
  val map = System.getProperties()
    .map { (k, v) ->
      val key = k.toString()
      val value = if (isSensitive(key)) "***REDACTED***" else v.toString()
      key to value
    }
    .toMap()
  SysProps(map)
}

data class SysProps(
  val properties: Map<String, String>,
)
