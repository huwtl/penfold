package org.huwtl.penfold.app.readstore.postgres

import org.huwtl.penfold.support.PostgresSpecification

import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.StaticQuery.interpolation

class PostgresTest extends PostgresSpecification {

  val database = newDatabase()

  "test embedded postgres" in {
    val count = database.withDynSession(sql"""SELECT count(data) from tasks""".as[Int].first)

    count must beEqualTo(0)
  }
}
