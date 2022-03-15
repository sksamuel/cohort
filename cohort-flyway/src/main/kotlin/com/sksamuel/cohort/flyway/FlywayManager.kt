package com.sksamuel.cohort.flyway

import com.sksamuel.cohort.db.DatabaseMigrationManager
import com.sksamuel.cohort.db.Migration
import org.flywaydb.core.Flyway
import javax.sql.DataSource

class FlywayManager(private val ds: DataSource) : DatabaseMigrationManager {

  override fun migrations() = runCatching {
    Flyway
      .configure()
      .dataSource(ds)
      .load()
      .info()
      .all()
      .map {
        Migration(
          it.script,
          it.description,
          it.checksum,
          it.installedBy,
          it.installedOn.toInstant(),
          it.version.toString(),
          it.state.displayName,
        )
      }
  }

}
