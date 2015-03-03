package org.huwtl.penfold.app.store.postgres

import org.specs2.mutable.Specification
import org.huwtl.penfold.app.DatabaseConfiguration
import javax.sql.DataSource

class PostgresConnectionPoolFactoryTest extends Specification {
  "create db connection pool" in {
    val factory = new PostgresConnectionPoolFactory
    factory.create(DatabaseConfiguration("url", "user", "pass", "org.postgresql.Driver", 15)) must beAnInstanceOf[DataSource]
  }
}
