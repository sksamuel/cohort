package com.sksamuel.cohort.db

import java.time.Instant

interface DatabaseMigrationManager {
  fun migrations(): Result<List<Migration>>
}

data class Migration(
  val script: String,
  val description: String,
  val checksum: String,
  val author: String,
  val timestamp: Instant,
  val version: String,
  val state: String
)
