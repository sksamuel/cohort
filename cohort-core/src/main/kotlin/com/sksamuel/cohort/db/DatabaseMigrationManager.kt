package com.sksamuel.cohort.db

import java.time.Instant

interface DatabaseMigrationManager {
  fun migrations(): Result<List<Migration>>
}

data class Migration(
  val script: String,
  val description: String,
  val checksum: Int,
  val installedBy: String,
  val installedOn: Instant,
  val version: String,
  val state: String
)
