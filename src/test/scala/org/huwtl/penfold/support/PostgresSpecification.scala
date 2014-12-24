package org.huwtl.penfold.support

import com.googlecode.flyway.core.Flyway
import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL
import org.specs2.mutable.Specification
import org.specs2.specification.{Fragments, Step}

import scala.slick.driver.JdbcDriver.backend.Database

trait PostgresSpecification extends Specification {
  sequential

  var postgres: EmbeddedPostgreSQL = null

  override def map(fs: => Fragments) = fs ^ Step(postgres.close())

  def newDatabase(): Database = {
    postgres = EmbeddedPostgreSQL.start()

    val dataSource = postgres.getPostgresDatabase

    val flyway = new Flyway
    flyway.setDataSource(dataSource)
    flyway.setLocations("readstore/migration")
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}