package org.huwtl.penfold.domain.model

sealed trait Status {
  val name: String
}

object Status {
  case object Waiting extends Status {
    val name = "waiting"
  }

  case object Ready extends Status {
    val name = "ready"
  }

  case object Started extends Status {
    val name = "started"
  }

  case object Closed extends Status {
    val name = "closed"
  }

  case object Cancelled extends Status {
    val name = "cancelled"
  }

  case object Archived extends Status {
    val name = "archived"
  }

  def from(str: String): Option[Status] = {
    str.toLowerCase match {
      case Waiting.name => Some(Waiting)
      case Ready.name => Some(Ready)
      case Started.name => Some(Started)
      case Closed.name => Some(Closed)
      case Cancelled.name => Some(Cancelled)
      case _ => None
    }
  }
}
