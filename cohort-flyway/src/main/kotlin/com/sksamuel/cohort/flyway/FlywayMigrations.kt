package com.sksamuel.cohort.flyway

import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.db.Migration
import org.flywaydb.core.Flyway
import java.time.Instant
import javax.sql.DataSource

class FlywayMigrations(private val ds: DataSource) : DatabaseMigrationManager {

  override fun migrations() = runCatching {
    Flyway
      .configure()
      .dataSource(ds)
      .load()
      .info()
      .all()
      .map {
        Migration(
          script = it.script,
          description = it.description,
          checksum = it.checksum.toString(),
          author = it.installedBy ?: "",
          timestamp = it.installedOn?.toInstant() ?: Instant.ofEpochMilli(0),
          // Repeatable migrations have a null version. Without the safe-call this NPEs (or
          // records the literal string "null") in the same way the pending-migration NPE
          // fixed in 5ea010d did.
          version = it.version?.toString() ?: "",
          state = it.state.displayName,
        )
      }
  }

}
