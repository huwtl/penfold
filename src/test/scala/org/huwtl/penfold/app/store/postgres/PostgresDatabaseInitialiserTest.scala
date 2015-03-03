package org.huwtl.penfold.app.store.postgres

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import com.googlecode.flyway.core.Flyway
import javax.sql.DataSource
import scala.slick.driver.JdbcDriver.backend.Database

class PostgresDatabaseInitialiserTest extends Specification with Mockito {
  "init database schema" in {
    val flyway = mock[Flyway]
    val dataSource = mock[DataSource]
    val database = new PostgresDatabaseInitialiser(None, flyway).init(dataSource)

    there was one(flyway).setLocations("db/migration/eventstore", "db/migration/readstore")
    there was one(flyway).migrate()
    database must beAnInstanceOf[Database]
  }

  "init database schema with custom migration location" in {
    val flyway = mock[Flyway]
    val dataSource = mock[DataSource]
    new PostgresDatabaseInitialiser(Some(CustomDbMigrationPath("/tmp/custom")), flyway).init(dataSource)

    there was one(flyway).setLocations("filesystem:/tmp/custom", "db/migration/eventstore", "db/migration/readstore")
  }
}
