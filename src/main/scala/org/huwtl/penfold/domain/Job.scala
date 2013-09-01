package org.huwtl.penfold.domain

import org.joda.time.DateTime.now
import org.joda.time.DateTime

case class Job(id: Id, jobType: JobType, cron: Option[Cron] = None, triggerDate: Option[DateTime] = None,
               status: Status = Status.Waiting, payload: Payload, created : Option[DateTime] = None,
               lastModified : Option[DateTime] = None) {
  val nextTriggerDate = triggerDate match {
    case Some(triggerDate) => triggerDate
    case _ => if (cron.isDefined) cron.get.nextExecutionDate else now()
  }
}

case class Id(value: String)

case class JobType(value: String)

case class Payload(content: Map[String, Any])

sealed trait Status {
  val name: String
}

object Status {
  case object Waiting extends Status {val name = "waiting"}
  case object Triggered extends Status {val name = "triggered"}
  case object Started extends Status {val name = "started"}
  case object Completed extends Status {val name = "completed"}

  def from(str : String): Status = {
    str.toLowerCase match {
      case Waiting.name => Waiting
      case Triggered.name => Triggered
      case Started.name => Started
      case Completed.name => Completed
    }
  }
}