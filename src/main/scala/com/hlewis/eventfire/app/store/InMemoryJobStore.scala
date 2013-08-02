package com.hlewis.eventfire.app.store

import scala.collection.mutable
import com.hlewis.eventfire.domain._
import com.hlewis.eventfire.domain.Job
import com.hlewis.eventfire.domain.Cron

class InMemoryJobStore extends JobStore {

  private val store = mutable.LinkedHashMap[String, Job](
    "job1" -> Job("job1", "test", Some(Cron("0", "*", "*", "*", "*", "*")), None, "waiting", Payload(Map("data" -> "value"))),
    "job2" -> Job("job2", "test", Some(Cron("0", "*", "*", "*", "*", "*")), None, "waiting", Payload(Map("data" -> "value")))
  )

  override def retrieveBy(id: String) = {
    store.get(id)
  }

  override def retrieveTriggered() = {
    store.values
      .filter(!_.nextTriggerDate.isAfterNow)
      .filter(_.status == "waiting")
      .toList
      .sortWith((job1, job2) => job1.nextTriggerDate.isAfter(job2.nextTriggerDate))
  }

  override def retrieveTriggeredBy(jobType: String) = {
    retrieveTriggered()
      .filter(_.jobType == jobType)
  }

  override def retrieveCompleted() = {
    retrieveWith("completed")
  }

  override def add(job: Job) = {
    store.put(job.id, job)
    job
  }

  override def update(job: Job) = {
    store.put(job.id, job)
    job
  }

  override def remove(job: Job) {
    store.remove(job.id)
  }

  private def retrieveWith(status: String) = {
    store.values.filter(_.status == status).toList.reverse
  }
}
