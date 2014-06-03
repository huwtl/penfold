package com.qmetric.penfold.app.store.jdbc

import javax.sql.DataSource
import com.googlecode.flyway.core.Flyway
import scala.slick.driver.JdbcDriver.backend.Database

class JdbcDatabaseInitialiser(flyway: Flyway = new Flyway) {
  def init(dataSource: DataSource) = {
    flyway.setDataSource(dataSource)
    flyway.migrate()

    Database.forDataSource(dataSource)
  }
}
