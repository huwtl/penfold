package org.huwtl.penfold.readstore

sealed trait QueryParamType {
  val name: String
}

object QueryParamType {
  case object StringType extends QueryParamType {
    override val name = "STRING"
  }
  case object NumericType extends QueryParamType {
    override val name = "NUMERIC"
  }

  def from(str: String): QueryParamType = {
    str.toUpperCase match {
      case StringType.name => StringType
      case NumericType.name => NumericType
      case _ => throw new IllegalArgumentException(s"unknown query param type: ${str}")
    }
  }
}