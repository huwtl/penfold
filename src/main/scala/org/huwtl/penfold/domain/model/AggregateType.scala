package org.huwtl.penfold.domain.model

sealed trait AggregateType {
  val name: String
}

object AggregateType {
  case object Job extends AggregateType {
    val name = "Job"
  }

  def from(str: String): Option[AggregateType] = {
    str.toLowerCase match {
      case Job.name => Some(Job)
      case _ => None
    }
  }
}
