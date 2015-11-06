package com.qmetric.penfold.domain.model

sealed trait CloseResultType {
  val name: String
}

object CloseResultType {
  case object Success extends CloseResultType {
    val name = "success"
  }

  case object Failure extends CloseResultType {
    val name = "failure"
  }

  def from(str: String): Option[CloseResultType] = {
    str.toLowerCase match {
      case Success.name => Some(Success)
      case Failure.name => Some(Failure)
      case _ => None
    }
  }
}