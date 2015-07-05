package org.huwtl.penfold.app.support.postgres

import javax.sql.DataSource

import org.huwtl.penfold.app.DatabaseConfiguration
import org.specs2.mutable.SpecificationWithJUnit

class PostgresConnectionPoolFactoryTest extends SpecificationWithJUnit {
  "create db connection pool" in {
    val factory = new PostgresConnectionPoolFactory
    factory.create(DatabaseConfiguration("url", "user", "pass", "org.postgresql.Driver", 15)) must beAnInstanceOf[DataSource]
  }
}
