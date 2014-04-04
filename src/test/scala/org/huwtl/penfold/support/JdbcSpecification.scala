package org.huwtl.penfold.support

import org.specs2.mutable.Specification
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.googlecode.flyway.core.Flyway
import scala.slick.driver.JdbcDriver.backend.Database

trait JdbcSpecification extends Specification {

  sequential

  val dataSource = new ComboPooledDataSource
  dataSource.setDriverClass("org.hsqldb.jdbcDriver")
  dataSource.setJdbcUrl("jdbc:hsqldb:mem:penfold;sql.syntax_mys=true")
  dataSource.setUser("sa")
  dataSource.setPassword("")

  val flyway = new Flyway
  flyway.setDataSource(dataSource)

  def newDatabase() = {
    flyway.clean()
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}