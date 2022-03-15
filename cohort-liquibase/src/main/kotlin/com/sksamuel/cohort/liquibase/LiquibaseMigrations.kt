package com.sksamuel.cohort.liquibase

import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.db.Migration
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.time.Instant
import javax.sql.DataSource

class LiquibaseMigrations(
  private val ds: DataSource,
  private val changelogFile: String,
) : DatabaseMigrationManager {

  override fun migrations(): Result<List<Migration>> = runCatching {
    ds.connection.use { conn ->

      val l = Liquibase(
        changelogFile,
        ClassLoaderResourceAccessor(),
        JdbcConnection(conn)
      )

      l.databaseChangeLog.changeSets.map { changeset ->
        Migration(
          script = changeset.filePath,
          description = changeset.description,
          checksum = changeset.storedCheckSum.toString(),
          author = changeset.author,
          timestamp = Instant.ofEpochMilli(0),
          version = "",
          state = "unknown",
        )
      }

    }
  }
}
