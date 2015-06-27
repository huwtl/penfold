package com.qmetric.penfold.support

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL
import com.qmetric.penfold.app.store.postgres.PostgresDatabaseInitialiser
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{Fragments, Step}

import scala.slick.driver.JdbcDriver.backend.Database

trait PostgresSpecification extends SpecificationWithJUnit {
  sequential

  var postgres: EmbeddedPostgreSQL = null

  override def map(fs: => Fragments) = fs ^ Step(postgres.close())

  def newDatabase(): Database = {
    if (postgres != null) {
      postgres.close()
    }

    postgres = EmbeddedPostgreSQL.start()

    val dataSource = postgres.getPostgresDatabase

    new PostgresDatabaseInitialiser(None).init(dataSource)
  }
}