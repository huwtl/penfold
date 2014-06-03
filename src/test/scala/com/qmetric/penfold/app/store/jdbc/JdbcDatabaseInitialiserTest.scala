package com.qmetric.penfold.app.store.jdbc

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.googlecode.flyway.core.Flyway
import javax.sql.DataSource
import scala.slick.driver.JdbcDriver.backend.Database

class JdbcDatabaseInitialiserTest extends Specification with Mockito {
  "init database schema" in {
    val flyway = mock[Flyway]
    val dataSource = mock[DataSource]
    val database = new JdbcDatabaseInitialiser(flyway).init(dataSource)

    there was one(flyway).migrate()
    database must beAnInstanceOf[Database]
  }
}
