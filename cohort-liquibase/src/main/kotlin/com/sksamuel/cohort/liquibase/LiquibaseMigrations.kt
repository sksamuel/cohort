package com.sksamuel.cohort.liquibase

import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.db.Migration
import liquibase.Liquibase
import liquibase.changelog.StandardChangeLogHistoryService
import liquibase.database.DatabaseFactory
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
         val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(conn))
         val liquibase = Liquibase(
            changelogFile,
            ClassLoaderResourceAccessor(),
            database
         )

         val service = StandardChangeLogHistoryService()
         service.database = database
         val appliedChangeSets = service.getRanChangeSets()
         val appliedIds = appliedChangeSets.map { "${it.id}:${it.author}" }.toSet()

         liquibase.databaseChangeLog.changeSets.map { changeset ->
            val changesetId = "${changeset.id}:${changeset.author}"
            val isApplied = appliedIds.contains(changesetId)

            val appliedChangeSet = if (isApplied) {
               appliedChangeSets.find { it.id == changeset.id && it.author == changeset.author }
            } else null

            Migration(
               script = changeset.filePath,
               description = changeset.description ?: "",
               checksum = changeset.storedCheckSum?.toString() ?: "",
               author = changeset.author,
               timestamp = appliedChangeSet?.let { Instant.ofEpochMilli(it.dateExecuted.time) } ?: Instant.ofEpochMilli(
                  0
               ),
               version = "",
               state = if (isApplied) "applied" else "pending"
            )

         }
      }

   }
}

