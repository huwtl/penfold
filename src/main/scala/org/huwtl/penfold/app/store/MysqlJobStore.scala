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
    id = Id(r.nextString()),
    jobType = JobType(r.nextString()),
    cron = r.nextStringOption().map(cron => Cron(cron)),
    triggerDate = r.nextTimestampOption().map(timestamp => new DateTime(timestamp)),
    status = Status.from(r.nextString()),
    payload = jobConverter.jobPayloadFrom(r.nextString()),
    created = Some(new DateTime(r.nextTimestamp())),
    lastModified = Some(new DateTime(r.nextTimestamp()))
  ))

  override def retrieveBy(id: Id) = {
    database.withSession {
      sql"""
        SELECT id, job_type, cron, trigger_date, status, payload, created, last_modified FROM jobs
          WHERE id = ${id.value}
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
        INSERT INTO jobs (id, job_type, cron, trigger_date, status, payload, created, last_modified) VALUES (
          ${job.id.value},
          ${job.jobType.value},
          ${job.cron.map(_.toString)},
          ${new Timestamp(job.nextTriggerDate.getMillis).toString},
          ${job.status.name},
          ${jobConverter.jsonFrom(job.payload)},
          ${new Timestamp(job.created.get.getMillis).toString},
          ${new Timestamp(job.lastModified.get.getMillis).toString}
        )
      """.execute
      job
    }
  }

  override def updateStatus(job: Job, status: Status) = {
    val rowsUpdated = sqlu"""
      UPDATE jobs SET status = ${status.name}
        WHERE id = ${job.id.value}
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
        DELETE FROM jobs WHERE id = ${job.id.value}
      """.first
    }
  }

  override def retrieve(status: Status) = {
    sql"""
      SELECT id, job_type, cron, trigger_date, status, payload, created, last_modified FROM jobs
        WHERE status = ${status.name}
        ORDER BY last_modified
    """.as[Job].list
  }
}
