package org.huwtl.penfold.domain.model

sealed trait Status {
  val name: String
  val level: Int
  def isBefore(status: Status) = status.level < level
}

object Status {
  case object Waiting extends Status {
    val name = "waiting"
    val level = 0
  }

  case object Ready extends Status {
    val name = "ready"
    val level = 1
  }

  case object Started extends Status {
    val name = "started"
    val level = 2
  }

  case object Cancelled extends Status {
    val name = "cancelled"
    val level = 3
  }

  case object Completed extends Status {
    val name = "completed"
    val level = 3
  }

  def from(str: String): Option[Status] = {
    str.toLowerCase match {
      case Waiting.name => Some(Waiting)
      case Ready.name => Some(Ready)
      case Started.name => Some(Started)
      case Completed.name => Some(Completed)
      case _ => None
    }
  }
}
