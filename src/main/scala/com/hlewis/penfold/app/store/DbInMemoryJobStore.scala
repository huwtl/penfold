package com.hlewis.penfold.app.store

import com.hlewis.penfold.domain._
import com.hlewis.penfold.domain.Job
import scala.slick.session.Database
import Database.threadLocalSession
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import org.joda.time.DateTime
import com.hlewis.penfold.app.support.JobJsonConverter
import java.sql.Timestamp

class DbInMemoryJobStore(database: Database, jobConverter: JobJsonConverter) extends JobStore {
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
      selectJobBy(id)
    }
  }

  override def triggerPendingJobs() {
    database.withSession {
      updateJobsWhereTriggeredDateInPast()
    }
  }

  override def add(job: Job) = {
    database.withSession {
      insertIntoJob(job)
      job
    }
  }

  override def update(job: Job) = {
    null
  }

  override def remove(job: Job) {
    database.withSession {
      deleteFromJob(job.id)
    }
  }

  override def retrieve(status: Status) = {
    null
  }

  private def selectJobBy(id: String) = {
    sql"""
      SELECT id, job_type, cron, trigger_date, status, payload FROM jobs
        WHERE id = $id
    """.as[Job].firstOption
  }

  private def updateJobsWhereTriggeredDateInPast() = {
    sqlu"""
      UPDATE jobs SET status = 'triggered'
        WHERE status = 'waiting'
        AND trigger_date < ${new Timestamp(DateTime.now().getMillis)}" +
    """.execute
  }

  private def deleteFromJob(id: String) = sqlu"""DELETE FROM jobs WHERE id = $id""".first

  private def insertIntoJob(job: Job) = {
    sqlu"""
      INSERT INTO jobs (id, job_type, cron, trigger_date, status, payload) VALUES (
        ${job.id},
        ${job.jobType},
        ${if (job.cron.isDefined) job.cron.toString else null},
        ${new Timestamp(job.nextTriggerDate.getMillis).toString},
        ${job.status.name},
        ${jobConverter.jsonFrom(job.payload)}
      )""".execute
  }
}


