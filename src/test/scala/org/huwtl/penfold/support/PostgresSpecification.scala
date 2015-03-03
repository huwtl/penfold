package org.huwtl.penfold.support

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL
import org.specs2.mutable.Specification
import org.specs2.specification.{Fragments, Step}

import scala.slick.driver.JdbcDriver.backend.Database
import org.huwtl.penfold.app.store.postgres.{CustomDbMigrationPath, PostgresDatabaseInitialiser}

trait PostgresSpecification extends Specification {
  sequential

  var postgres: EmbeddedPostgreSQL = null

  override def map(fs: => Fragments) = fs ^ Step(postgres.close())

  def newDatabase(): Database = {
    if (postgres != null) {
      postgres.close()
    }

    postgres = EmbeddedPostgreSQL.start()

    val dataSource = postgres.getPostgresDatabase

    new PostgresDatabaseInitialiser(Some(CustomDbMigrationPath("/Users/hlewis/penfold_db"))).init(dataSource)
  }
}