package org.huwtl.penfold.app.readstore.postgres

import com.opentable.db.postgres.embedded.EmbeddedPostgreSQL
import org.specs2.mutable.Specification

class PostgresTest extends Specification {

  "test embedded postgres" in {
    val pg = EmbeddedPostgreSQL.start()
    val c = pg.getPostgresDatabase.getConnection
    val s = c.createStatement()
    val rs = s.executeQuery("SELECT 1")

    rs.next()
    val int = rs.getInt(1)
    val next = rs.next()

    pg.close()

    int must beEqualTo(1)
    next must beFalse
  }
}
