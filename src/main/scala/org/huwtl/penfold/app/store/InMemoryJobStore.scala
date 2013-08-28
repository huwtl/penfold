package org.huwtl.penfold.app.store

import scala.collection.mutable
import org.huwtl.penfold.domain._
import org.huwtl.penfold.domain.Job
import org.huwtl.penfold.domain.Cron

class InMemoryJobStore extends JobStore {
  private val store = mutable.LinkedHashMap[String, Job](
    "job1" -> Job("job1", "test", Some(Cron("0 * * * * * *")), None, Status.Waiting, Payload(Map("data" -> "value"))),
    "job2" -> Job("job2", "test", Some(Cron("0 * * * * * *")), None, Status.Waiting, Payload(Map("data" -> "value")))
  )

  override def retrieveBy(id: String) = {
    store.get(id)
  }

  override def triggerPendingJobs() {
    store.values
      .filter(!_.nextTriggerDate.isAfterNow)
      .filter(_.status == Status.Waiting)
      .foreach(job => store.put(job.id, Job(job.id, job.jobType, job.cron, job.triggerDate, Status.Triggered, job.payload)))
  }

  override def add(job: Job) = {
    store.put(job.id, job)
    job
  }

  override def updateStatus(job: Job, status: Status) = {
    store.put(job.id, job)
    job
  }

  override def remove(job: Job) {
    store.remove(job.id)
  }

  override def retrieve(status: Status) = {
    store.values
      .filter(_.status == status)
      .toList
      .sortWith((job1, job2) => job1.nextTriggerDate.isAfter(job2.nextTriggerDate))
  }
}
