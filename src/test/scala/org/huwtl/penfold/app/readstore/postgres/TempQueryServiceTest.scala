package org.huwtl.penfold.app.readstore.postgres

import org.specs2.specification.Scope
import org.huwtl.penfold.support.PostgresSpecification
import scala.slick.driver.JdbcDriver.backend.Database
import Database.dynamicSession
import scala.slick.jdbc.{StaticQuery => Q}
import scala.slick.jdbc.StaticQuery.interpolation

class TempQueryServiceTest extends PostgresSpecification {
  sequential

  val database = newDatabase()

  class context extends Scope {
    val trackingKey = "testKey"

    database.withDynSession {
      sqlu"""DELETE FROM trackers""".execute
    }

    val service = new TempQueryService(database)
  }

  "do something" in new context {
    service.get must beEqualTo(("", Some("")))
  }

  "do something2" in new context {
    service.get2 must beEqualTo(("", Some("")))
  }
}
