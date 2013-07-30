package com.hlewis.eventfire.domain

import org.joda.time.DateTime.now
import org.joda.time.DateTime

case class Job(id: String, jobType: String, cron: Option[Cron], triggerDate: Option[DateTime], status: String = "waiting", payload: Payload) {
  val nextTriggerDate = cron match {
    case Some(cron) => cron.nextExecutionDate
    case _ => triggerDate getOrElse now()
  }
}

case class Payload(content: Map[String, Any])