package com.qmetric.penfold.support

import org.specs2.mutable.Specification
import com.googlecode.flyway.core.Flyway
import scala.slick.driver.JdbcDriver.backend.Database
import org.specs2.specification.{Step, Fragments}
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.util.UUID

trait JdbcSpecification extends Specification {
  sequential

  val dataSource = new ComboPooledDataSource
  dataSource.setDriverClass("org.hsqldb.jdbcDriver")
  dataSource.setJdbcUrl(s"jdbc:hsqldb:mem:${UUID.randomUUID().toString};sql.syntax_mys=true")
  dataSource.setUser("sa")
  dataSource.setPassword("")

  val flyway = new Flyway
  flyway.setDataSource(dataSource)

  override def map(fs: => Fragments) = fs ^ Step(dataSource.close())

  def newDatabase() = {
    flyway.clean()
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}