package com.qmetric.penfold.app.store.jdbc

import org.specs2.mutable.Specification
import com.qmetric.penfold.app.JdbcConnectionPool
import javax.sql.DataSource

class JdbcConnectionPoolFactoryTest extends Specification {
  "create jdbc connection pool" in {
    val factory = new JdbcConnectionPoolFactory
    factory.create(JdbcConnectionPool("url", "user", "pass", "org.hsqldb.jdbcDriver", 15)) must beAnInstanceOf[DataSource]
  }
}
