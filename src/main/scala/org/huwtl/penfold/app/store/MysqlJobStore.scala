package org.huwtl.penfold.app.store

import org.huwtl.penfold.domain._
import org.huwtl.penfold.domain.Job
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.joda.time.DateTime
import org.huwtl.penfold.app.support.JobJsonConverter
import java.sql.Timestamp
import org.huwtl.penfold.domain.exceptions.JobUpdateConflictException

class MysqlJobStore(database: Database, jobConverter: JobJsonConverter) extends JobStore {
  implicit val getJobFromRow = GetResult(r => Job(
    r.nextString(),
    r.nextString(),
    r.nextStringOption().map(cron => Cron(cron)),
    r.nextTimestampOption().map(timestamp => new DateTime(timestamp)),
    Status.from(r.nextString()),
    jobConverter.jobPayloadFrom(r.nextString())
  ))

  override def retrieveBy(id: String) = {
    database.withSession {
      sql"""
        SELECT id, job_type, cron, trigger_date, status, payload FROM jobs
          WHERE id = $id
      """.as[Job].firstOption
    }
  }

  override def triggerPendingJobs() {
    database.withSession {
      sqlu"""
        UPDATE jobs SET status = 'triggered'
          WHERE status = 'waiting'
          AND trigger_date > ${new Timestamp(DateTime.now().getMillis)}
      """.execute
    }
  }

  override def add(job: Job) = {
    database.withSession {
      sqlu"""
        INSERT INTO jobs (id, job_type, cron, trigger_date, status, payload) VALUES (
          ${job.id},
          ${job.jobType},
          ${if (job.cron.isDefined) job.cron.get.toString else null},
          ${new Timestamp(job.nextTriggerDate.getMillis).toString},
          ${job.status.name},
          ${jobConverter.jsonFrom(job.payload)}
        )
      """.execute
      job
    }
  }

  override def updateStatus(job: Job, status: Status) = {
    val rowsUpdated = sqlu"""
      UPDATE jobs SET status = ${status.name}
        WHERE id = ${job.id}
        AND status = ${job.status.name}
    """.first

    if (rowsUpdated == 0) {
      throw JobUpdateConflictException(s"Job update conflict for ${job.id}")
    }

    Job(job.id, job.jobType, job.cron, Some(job.nextTriggerDate), status, job.payload)
  }

  override def remove(job: Job) {
    database.withSession {
      sqlu"""
        DELETE FROM jobs WHERE id = ${job.id}
      """.first
    }
  }

  override def retrieve(status: Status) = {
    sql"""
      SELECT id, job_type, cron, trigger_date, status, payload FROM jobs
        WHERE status = ${status.name}
    """.as[Job].list
  }
}


