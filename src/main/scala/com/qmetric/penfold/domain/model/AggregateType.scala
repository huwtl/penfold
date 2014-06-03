package com.qmetric.penfold.domain.model

sealed trait AggregateType {
  val name: String
}

object AggregateType {
  case object Task extends AggregateType {
    val name = "Task"
  }

  def from(str: String): Option[AggregateType] = {
    str match {
      case Task.name => Some(Task)
      case _ => None
    }
  }
}
