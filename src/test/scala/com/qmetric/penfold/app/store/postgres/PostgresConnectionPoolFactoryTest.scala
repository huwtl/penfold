package com.qmetric.penfold.app.store.postgres

import org.specs2.mutable.Specification
import com.qmetric.penfold.app.DatabaseConfiguration
import javax.sql.DataSource
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PostgresConnectionPoolFactoryTest extends Specification {
  "create db connection pool" in {
    val factory = new PostgresConnectionPoolFactory
    factory.create(DatabaseConfiguration("url", "user", "pass", "org.postgresql.Driver", 15)) must beAnInstanceOf[DataSource]
  }
}
