package org.huwtl.penfold.app.store

import org.specs2.mutable.Specification
import com.mchange.v2.c3p0.ComboPooledDataSource
import scala.slick.session.Database
import com.googlecode.flyway.core.Flyway
import org.huwtl.penfold.app.support.JobJsonConverter
import org.huwtl.penfold.domain.{Cron, Payload, Status, Job}
import org.joda.time.DateTime
import org.specs2.matcher.DataTables

class MysqlJobStoreTest extends Specification with DataTables {
  val dataSource = new ComboPooledDataSource
  dataSource.setDriverClass("org.hsqldb.jdbcDriver")
  dataSource.setJdbcUrl("jdbc:hsqldb:mem:penfold;sql.syntax_mys=true")
  dataSource.setUser("sa")
  dataSource.setPassword("")

  val flyway = new Flyway
  flyway.setDataSource(dataSource)
  flyway.migrate()

  val store = new MysqlJobStore(Database.forDataSource(dataSource), new JobJsonConverter)

  "add job to store" in {
    "job"                                                                                                                   |>
    Job("1", "type", None, Some(new DateTime(2013, 8, 1, 12, 0, 0)), Status.Waiting, Payload(Map()))                        |
    Job("2", "type", Some(Cron("* * * * * * *")), Some(new DateTime(2013, 8, 1, 12, 0, 0)), Status.Waiting, Payload(Map())) | { (job) =>
      store.add(job)
      job must beEqualTo(store.retrieveBy(job.id).get)
    }
  }
}
